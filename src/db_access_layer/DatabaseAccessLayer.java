package db_access_layer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Stream;




public class DatabaseAccessLayer {
	
	public static final DatabaseAccessLayer SINGLETON = new DatabaseAccessLayer();
	private Connection connector;
	

	private PreparedStatement ClusterIDSelection;
	private PreparedStatement RelatedFeaturesInsertion;

		
	 public static DatabaseAccessLayer getInstance() { 
	        return DatabaseAccessLayer.SINGLETON;
	} 
    public void initializeConnector() throws Exception
    {
    	Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        this.connector = DriverManager.getConnection(Utilities.Constants.DATABASE);       

        this.ClusterIDSelection = this.connector
				.prepareStatement("SELECT file.project_id, method.id, cluster.clusterID from file  INNER JOIN (cluster  INNER JOIN method )on (file.id = method.file_id and  method.id = cluster.methodID) ORDER BY file.project_id ASC ");
        
		this.connector.setAutoCommit(false);
    }
    public void initializeConnectorToPopulateRelatedFeatures() throws Exception
    {
    	Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        this.connector = DriverManager.getConnection(Utilities.Constants.DATABASE);       

        this.RelatedFeaturesInsertion = this.connector
				.prepareStatement("INSERT INTO related_features VALUES (0,?,?)");
        
		this.connector.setAutoCommit(false);
    }
    
   
    public void closeConnector() throws SQLException{
    	this.connector.close();
    }
 
	
	public ArrayList<String> getClusterIDsPerProject() throws SQLException {
		
		ArrayList<String> clusterIDList = new ArrayList<String>();	
		ResultSet resultSet = ClusterIDSelection.executeQuery();
		int previousProjectID = -1;
		String s = "";
		while(resultSet.next())
		{
			int projectID = resultSet.getInt(1);
			int methodID = resultSet.getInt(2);
			int clusterID = resultSet.getInt(3);			
			
			if(projectID == previousProjectID || previousProjectID == -1)
			{
				s = s.concat(clusterID + " ");
			}			
			else
			{				
				clusterIDList.add(s);
				s = "";
			}
			previousProjectID = projectID;
		}
		clusterIDList.add(s);
		ClusterIDSelection.close();
		return clusterIDList;
	}
	public void populateRelatedFeaturesTable(ArrayList<String> relatedFeaturesList) throws NumberFormatException, SQLException {
		
		int featureID = 0;
		for(String relatedClusterIDs: relatedFeaturesList)
		{			
			String[] clusterIDs = relatedClusterIDs.split("\\s+");
			for(int i = 0; i < clusterIDs.length; i++)
			{
				RelatedFeaturesInsertion.setInt(1, featureID);   
				RelatedFeaturesInsertion.setInt(2, Integer.parseInt(clusterIDs[i]));  				
				RelatedFeaturesInsertion.addBatch(); 
			}
			featureID += 1;
			 
		}
		
		int[] inserted = RelatedFeaturesInsertion.executeBatch();
		RelatedFeaturesInsertion.close();
		connector.commit();
		
	}
	
	

	
}
