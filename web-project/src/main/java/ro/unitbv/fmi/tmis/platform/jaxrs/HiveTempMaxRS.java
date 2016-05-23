package ro.unitbv.fmi.tmis.platform.jaxrs;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import ro.unitbv.fmi.tmis.platform.dao.RegionDAO;
import ro.unitbv.fmi.tmis.platform.dao.TempMaxAvgEachYearDAO;
import ro.unitbv.fmi.tmis.platform.hive.dao.TempMaxDAO;
import ro.unitbv.fmi.tmis.platform.model.Region;
import ro.unitbv.fmi.tmis.platform.model.TempMaxAvgEachYear;

@Path("/api")
public class HiveTempMaxRS {
	@Inject
	private TempMaxDAO tempMaxDAO;
	@Inject
	private TempMaxAvgEachYearDAO tempMaxAvgEachYearDAO;
	@Inject
	private RegionDAO regionDAO;

	@DELETE
	@Path("/db/tempMaxTable")
	public void deleteTempMaxTable(
			@NotNull(message = "Database name must not be null") @QueryParam("dbName") String dbName) {
		System.out
				.println("Try to delete a table with name: [temp_max] in database ["
						+ dbName + "]");
		try {
			tempMaxDAO.dropTable(dbName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@PUT
	@Path("/db/tempMaxTable")
	public void createTempMaxTable(
			@NotNull(message = "Database name must not be null") @QueryParam("dbName") String dbName) {
		System.out
				.println("Try to create a table with name: [temp_max] in database ["
						+ dbName + "]");
		try {
			tempMaxDAO.createTable(dbName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/db/loadTempMaxData")
	public void loadTempMaxData(
			@NotNull(message = "Region id must not be null") @QueryParam("regionId") Long regionId,
			@NotNull(message = "Database name must not be null") @QueryParam("regionId") String dbName) {
		System.out.println("Try to load data into hive for region with id ["
				+ regionId + "]");
		try {
			Region region = regionDAO.getRegionById(regionId);

			tempMaxDAO.loadDataIntoTable(dbName,
					"/user/root/extracted-data/temp-max/" + region.getName(),
					regionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void extractMaxTempAvgEachYear(String dbName, Region region, int startYear,
			int endYear) throws SQLException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar startMonth = Calendar.getInstance();
		startMonth.set(startYear, 0, 1);
		Calendar endMonth = Calendar.getInstance();
		endMonth.set(startYear, 0, 31);

		for (int year = startYear; year <= endYear; year++) {
			for (int month = 1; month <= 12; month++) {
				System.out
						.println("Try to extract temp-max average for month with number ["
								+ month + "] in year [" + year + "]");
				double avgResult = tempMaxDAO.getAveragePerMonthEachYear(
						region.getIdRegion(), dbName,
						dateFormat.format(startMonth.getTime()),
						dateFormat.format(endMonth.getTime()));

				TempMaxAvgEachYear entity = new TempMaxAvgEachYear(year, month,
						avgResult, region);
				System.out.println("Result: " + avgResult);
				tempMaxAvgEachYearDAO.insertTempMaxAvgEachYear(entity);
				startMonth.add(Calendar.MONTH, 1);
				endMonth.add(Calendar.MONTH, 1);
			}
		}
	}

	@GET
	@Path("/db/tempMax/month/avg")
	public void getMaxTempAvgPerMonth(
			@NotNull(message = "Database name must not be null") @QueryParam("regionId") String dbName,
			@NotNull(message = "Region id must not be null") @QueryParam("regionId") Long regionId,
			@NotNull(message = "Each year option must not be null") @QueryParam("eachYear") Boolean eachYear) {

		Region region = regionDAO.getRegionById(regionId);
		try {
			if (eachYear) {
				extractMaxTempAvgEachYear(dbName ,region, region.getStartYear(),
						region.getEndYear());
			} else {
//				precipitationDAO.getAveragePerMonthAllYears(DB_NAME,
//						region.getStartYear(), region.getEndYear());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}