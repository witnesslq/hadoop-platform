package ro.unitbv.fmi.tmis.platform.jaxrs;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import ro.unitbv.fmi.tmis.platform.dao.QueryDAO;
import ro.unitbv.fmi.tmis.platform.dao.QueryUsedDAO;
import ro.unitbv.fmi.tmis.platform.dao.RegionDAO;
import ro.unitbv.fmi.tmis.platform.dao.TempMinAvgEachYearDAO;
import ro.unitbv.fmi.tmis.platform.exception.AlreadyExistException;
import ro.unitbv.fmi.tmis.platform.hive.dao.TempMinDAO;
import ro.unitbv.fmi.tmis.platform.hive.dto.TempMinAvgEachYearDTO;
import ro.unitbv.fmi.tmis.platform.model.Query;
import ro.unitbv.fmi.tmis.platform.model.Region;
import ro.unitbv.fmi.tmis.platform.model.TempMinAvgEachYear;
import ro.unitbv.fmi.tmis.platform.model.UsedQuery;

@Path("/api")
public class HiveTempMinRS {
	@Inject
	private TempMinDAO tempMinDAO;
	@Inject
	private RegionDAO regionDAO;
	@Inject
	private TempMinAvgEachYearDAO tempMinAvgEachYearDAO;
	@Inject
	private QueryDAO queryDAO;
	@Inject
	private QueryUsedDAO queryUsedDAO;

	@DELETE
	@Path("/db/tempMinTable")
	public void deleteTempMinTable(
			@NotNull(message = "Database name must not be null") @QueryParam("dbName") String dbName) {
		System.out
				.println("Try to delete a table with name: [temp_min] in database ["
						+ dbName + "]");
		try {
			tempMinDAO.dropTable(dbName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@PUT
	@Path("/db/tempMinTable")
	public void createTempMinTable(
			@NotNull(message = "Database name must not be null") @QueryParam("dbName") String dbName) {
		System.out
				.println("Try to create a table with name: [temp_min] in database ["
						+ dbName + "]");
		try {
			tempMinDAO.createTable(dbName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@POST
	@Path("/db/loadTempMinData")
	public void loadTempMinData(
			@NotNull(message = "Region id must not be null") @QueryParam("regionId") Long regionId,
			@NotNull(message = "Database name must not be null") @QueryParam("dbName") String dbName) {
		System.out.println("Try to load data into hive for region with id ["
				+ regionId + "]");
		try {
			Region region = regionDAO.getRegionById(regionId);

			tempMinDAO.loadDataIntoTable(dbName,
					"/user/root/extracted-data/temp-min/" + region.getName(),
					regionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void extractPrecipitationAvgEachYear(String dbName, Region region,
			int startYear, int endYear) throws SQLException,
			AlreadyExistException {
		Query query = queryDAO.getQueryByName("TempMinQuery");

		List<UsedQuery> usedQuerys = queryUsedDAO
				.getUsedUsedQueryByRegionAndQuery(region.getIdRegion(),
						query.getIdQuery());

		if (usedQuerys != null && usedQuerys.size() > 0) {
			for (UsedQuery uq : usedQuerys) {
				if (uq.getSuccessed() != null
						&& uq.getSuccessed() == Boolean.TRUE) {
					throw new AlreadyExistException("Query [" + query.getName()
							+ "] was executed already");
				}
			}
		}

		UsedQuery usedQuery = new UsedQuery();
		Date startTime = new Date();

		try {
			usedQuery.setQuery(query);
			usedQuery.setRegion(region);
			usedQuery.setRunning(true);

			queryUsedDAO.insertUsedQuery(usedQuery);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar startMonth = Calendar.getInstance();
			startMonth.set(startYear, 0, 1);
			Calendar endMonth = Calendar.getInstance();
			endMonth.set(startYear, 0, 31);

			if (region.getType().equals("turism")) {
				for (int year = startYear; year <= endYear; year++) {
					for (int month = 1; month <= 12; month++) {
						System.out
								.println("Try to extract average for month with number ["
										+ month + "] in year [" + year + "]");
						TempMinAvgEachYearDTO avgResult = tempMinDAO
								.getAveragePerMonthEachYear(
										region.getIdRegion(),
										dbName,
										dateFormat.format(startMonth.getTime()),
										dateFormat.format(endMonth.getTime()));

						TempMinAvgEachYear entity = new TempMinAvgEachYear(
								year, month, avgResult.getAvg(),
								avgResult.getMin(), region);
						tempMinAvgEachYearDAO.insertTempMinAvgEachYear(entity);
						startMonth.add(Calendar.MONTH, 1);
						endMonth.add(Calendar.MONTH, 1);
					}
				}
			} else if (region.getType().equals("prediction")) {
				for (int year = startYear; year <= endYear; year += 10) {
					for (int month = 1; month <= 12; month++) {
						System.out
								.println("Try to extract average for month with number ["
										+ month + "] in year [" + year + "]");
						TempMinAvgEachYearDTO avgResult = tempMinDAO
								.getAveragePerMonthEachYear(
										region.getIdRegion(),
										dbName,
										dateFormat.format(startMonth.getTime()),
										dateFormat.format(endMonth.getTime()));

						TempMinAvgEachYear entity = new TempMinAvgEachYear(
								year, month, avgResult.getAvg(),
								avgResult.getMin(), region);
						tempMinAvgEachYearDAO.insertTempMinAvgEachYear(entity);
						startMonth.add(Calendar.MONTH, 1);
						endMonth.add(Calendar.MONTH, 1);
					}
					startMonth.add(Calendar.YEAR, 9);
					endMonth.add(Calendar.YEAR, 9);
				}
			}
			queryUsedDAO.updateUsedQuery(usedQuery.getIdUsedQuery(),
					new Date().getTime() - startTime.getTime(), true, false);
		} catch (Exception exc) {
			queryUsedDAO.updateUsedQuery(usedQuery.getIdUsedQuery(),
					new Date().getTime() - startTime.getTime(), false, false);
			throw exc;
		}
	}

	@GET
	@Path("/db/tempMin/month/avg")
	public void getAvgPerMonth(
			@NotNull(message = "Region id must not be null") @QueryParam("regionId") Long regionId,
			@NotNull(message = "Database name must not be null") @QueryParam("dbName") String dbName,
			@NotNull(message = "Each year option must not be null") @QueryParam("eachYear") Boolean eachYear)
			throws AlreadyExistException {

		Region region = regionDAO.getRegionById(regionId);
		System.out.println("Region name: " + region.getName());
		// TO DO Get start and nd year...
		try {
			if (eachYear) {
				extractPrecipitationAvgEachYear(dbName, region,
						region.getStartYear(), region.getEndYear());
			} else {
				// precipitationDAO.getAveragePerMonthAllYears(DB_NAME,
				// region.getStartYear(), region.getEndYear());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
