using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WFU_Eye_Mockup
{
	public interface MapProvider
	{
		List<Personnel> MedicalPersonnel { get; }
		List<Personnel> SecurityPersonnel { get; }
		List<Personnel> AllPersonnel { get; }
		List<Personnel> PersonnelByDesignation(PersonnelDesignation designation);

		List<Incident> MedicalIncidents { get; }
		List<Incident> SecurityIncidents { get; }
		List<Incident> AllCurrentIncidents { get; }
		List<Incident> IncidentsByType(PersonnelDesignation types);

		//the top-left and bottom-right limits of the map's range
		decimal LatitudeStart { get; }
		decimal LatitudeEnd { get; }
		decimal LongitudeStart { get; }
		decimal LongitudeEnd { get; }
	}
}
