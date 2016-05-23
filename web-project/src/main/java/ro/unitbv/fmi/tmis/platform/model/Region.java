package ro.unitbv.fmi.tmis.platform.model;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class Region {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idRegion")
	private Long idRegion;

	@Column(unique = true)
	private String name;
	@Column
	private double minLat;
	@Column
	private double maxLat;
	@Column
	private double minLon;
	@Column
	private double maxLon;
	@Column
	private int startYear;
	@Column
	private int endYear;

	@OneToMany(mappedBy = "region", fetch = FetchType.EAGER)
	private List<PrecipitationAvgEachYear> precipitationAvgEachYear;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "region_query", joinColumns = @JoinColumn(name = "idRegion", referencedColumnName = "idRegion"), inverseJoinColumns = @JoinColumn(name = "idQuery", referencedColumnName = "idQuery"))
	private Set<Query> querys;

	/*
	 * @Column(name = "points", columnDefinition = "json")
	 * 
	 * @Convert(converter = PointsConverter.class)
	 * 
	 * @JsonProperty private Points coords;
	 */

	// private Country country;

	public Long getIdRegion() {
		return idRegion;
	}

	public void setIdRegion(Long idRegion) {
		this.idRegion = idRegion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMinLat() {
		return minLat;
	}

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public void setMinLon(double minLon) {
		this.minLon = minLon;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public void setMaxLon(double maxLon) {
		this.maxLon = maxLon;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public List<PrecipitationAvgEachYear> getPrecipitationAvgEachYear() {
		return precipitationAvgEachYear;
	}

	public void setPrecipitationAvgEachYear(
			List<PrecipitationAvgEachYear> precipitationAvgEachYear) {
		this.precipitationAvgEachYear = precipitationAvgEachYear;
	}

	public Set<Query> getQuerys() {
		return querys;
	}

	public void setQuerys(Set<Query> querys) {
		this.querys = querys;
	}

	/*
	 * public Points getCoords() { return coords; }
	 * 
	 * public void setCoords(Points coords) { this.coords = coords; }
	 */

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "id_country") public Country getCountry() { return
	 * country; }
	 * 
	 * public void setCountry(Country country) { this.country = country; }
	 */
}
