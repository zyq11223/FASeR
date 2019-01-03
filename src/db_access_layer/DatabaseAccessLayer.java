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

import DataObjects.Method;




public class DatabaseAccessLayer {
	
	public static final DatabaseAccessLayer SINGLETON = new DatabaseAccessLayer();
	private Connection connector;
	

	private PreparedStatement ClusterIDSelection;
	private PreparedStatement RelatedFeaturesInsertion;
	private PreparedStatement MethodSelection;
	private PreparedStatement APICallsSelection;
	private PreparedStatement RelatedFeaturesSelection;
	private PreparedStatement ClustersBelongingToFeaturesSelection;


		
	 public static DatabaseAccessLayer getInstance() { 
	        return DatabaseAccessLayer.SINGLETON;
	} 
    public void initializeConnector() throws Exception
    {
    	Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        this.connector = DriverManager.getConnection(Utilities.Constants.DATABASE);
        this.ClusterIDSelection = this.connector
				.prepareStatement("SELECT file.project_id, method.id, cluster.clusterID from file  INNER JOIN (cluster  INNER JOIN method )on (file.id = method.file_id and  method.id = cluster.methodID) ORDER BY file.project_id,cluster.clusterID ASC ");
        this.APICallsSelection = this.connector
				.prepareStatement("SELECT file.project_id, api_call.api_call_index_id FROM api_call inner join method on api_call.host_method_id = method.id inner join file on method.file_id = file.id where api_call.api_name NOT in ('Log','Intent','Toast') ");
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
		ArrayList<Integer> clusterIDsForProject = new ArrayList<Integer>();
		while(resultSet.next())
		{
			int projectID = resultSet.getInt(1);
			int methodID = resultSet.getInt(2);
			int clusterID = resultSet.getInt(3);			
			
			if(projectID == previousProjectID || previousProjectID == -1)
			{
				if(!clusterIDsForProject.contains(clusterID))
				{
					clusterIDsForProject.add(clusterID);
				}
				//s = s.concat(clusterID + " ");
			}			
			else
			{		
				String clusterTransaction = createString(clusterIDsForProject);
				clusterIDList.add(clusterTransaction);
				clusterIDsForProject.clear();
				clusterIDsForProject.add(clusterID);
				//s = "";
				//s = s.concat(clusterID + " ");
				
			}
			previousProjectID = projectID;
		}
		String clusterTransaction = createString(clusterIDsForProject);
		clusterIDList.add(clusterTransaction);
		ClusterIDSelection.close();
		return clusterIDList;
	}
	private String createString(ArrayList<Integer> clusterIDsForProject) {
		String result = "";
		for(int i:clusterIDsForProject)
		{
			result = result.concat(i + " ");
		}
		
		return result.trim();
	}
	public void populateRelatedFeaturesTable(ArrayList<String> relatedFeaturesList, int minDepth) throws NumberFormatException, SQLException {
		
		int featureID = 1;
		for(String relatedClusterIDs: relatedFeaturesList)
		{			
			String[] clusterIDs = relatedClusterIDs.split("\\s+");
			if(!(clusterIDs.length < minDepth))
			{
				for(int i = 0; i < clusterIDs.length; i++)
				{
					RelatedFeaturesInsertion.setInt(1, featureID);   
					RelatedFeaturesInsertion.setInt(2, Integer.parseInt(clusterIDs[i]));  				
					RelatedFeaturesInsertion.addBatch(); 
				}
				featureID += 1;
			}
		}
		
		int[] inserted = RelatedFeaturesInsertion.executeBatch();
		RelatedFeaturesInsertion.close();
		connector.commit();
		
	}
	public void initializeConnectorToDisplayMethodBodies() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        this.connector = DriverManager.getConnection(Utilities.Constants.DATABASE);       

        this.MethodSelection = this.connector
				.prepareStatement("SELECT cluster.clusterID, method.id, method.name,method.from_line_num, method.to_line_num, file.file_name, file.project_id FROM `cluster` inner join method on method.id = cluster.methodID inner join file on file.id=method.file_id where clusterID = ? ");
        this.APICallsSelection = this.connector
				.prepareStatement("SELECT api_name, api_usage FROM api_call where host_method_id = ?");
		this.connector.setAutoCommit(false);
		
	}
	public ArrayList<Method> getMethods(int clusterID) throws SQLException {
		ArrayList<Method> methodIDsList = new ArrayList<Method>();
		
		MethodSelection.setInt(1,clusterID);
		ResultSet resultSet = MethodSelection.executeQuery();
		while(resultSet.next())
    	{ 	
			Method m = new Method();
			m.id = resultSet.getInt(2);			
			m.name = resultSet.getString(3);
			m.from_line_num = resultSet.getInt(4);
			m.to_line_num = resultSet.getInt(5);
			m.file_name = resultSet.getString(6);
			m.projectID = resultSet.getInt(7);
			
			methodIDsList.add(m);
    	}		
		MethodSelection.close();
		return methodIDsList;
	}
	
	public Method getFirstMethod(int clusterID) throws SQLException {
		Method m = new Method();
		
		MethodSelection.setInt(1,clusterID);
		ResultSet resultSet = MethodSelection.executeQuery();
		if(resultSet.first())
    	{ 	
			
			m.id = resultSet.getInt(2);			
			m.name = resultSet.getString(3);
			m.from_line_num = resultSet.getInt(4);
			m.to_line_num = resultSet.getInt(5);
			m.file_name = resultSet.getString(6);
			m.projectID = resultSet.getInt(7);
			m.clusterID = clusterID;
			
			
			
    	}		
		//MethodSelection.close();
		return m;
	}
	
	public ArrayList<String> getMethodAPICalls(Method m) throws SQLException {
		ArrayList<String> apiCallsList = new ArrayList<String>();
		
		this.APICallsSelection.setInt(1,m.id);
		ResultSet resultSet = APICallsSelection.executeQuery();
		while(resultSet.next())
    	{ 	
			
			String name = resultSet.getString(1);			
			String usage = resultSet.getString(2);
			
			
			apiCallsList.add(name + "." + usage);
    	}		
		//APICallsSelection.close();
		return apiCallsList;
	}
	public ArrayList<String> getAPICallsPerProject() throws SQLException {
		ArrayList<String> apiCallIndexIDList = new ArrayList<String>();	
		ResultSet resultSet = APICallsSelection.executeQuery();
		int previousProjectID = -1;
		String s = "";
		ArrayList<Integer> apiCallIndexIDsForProject = new ArrayList<Integer>();
		while(resultSet.next())
		{
			int projectID = resultSet.getInt(1);
			//int methodID = resultSet.getInt(2);
			int apiIndexID = resultSet.getInt(2);			
			
			if(projectID == previousProjectID || previousProjectID == -1)
			{
				if(!apiCallIndexIDsForProject.contains(apiIndexID))
				{
					apiCallIndexIDsForProject.add(apiIndexID);
				}
				//s = s.concat(clusterID + " ");
			}			
			else
			{		
				String apiCallTransaction = createString(apiCallIndexIDsForProject);
				apiCallIndexIDList.add(apiCallTransaction);
				apiCallIndexIDsForProject.clear();
				apiCallIndexIDsForProject.add(apiIndexID);
				//s = "";
				//s = s.concat(clusterID + " ");
				
			}
			previousProjectID = projectID;
		}
		String clusterTransaction = createString(apiCallIndexIDsForProject);
		apiCallIndexIDList.add(clusterTransaction);
		APICallsSelection.close();
		return apiCallIndexIDList;
	}
	public void initializeConnectorToRetrieveRelatedFeatures() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        this.connector = DriverManager.getConnection(Utilities.Constants.DATABASE);       

        this.RelatedFeaturesSelection = this.connector
				.prepareStatement("SELECT id, feature_id FROM related_features where cluster_id = ? ");
        this.ClustersBelongingToFeaturesSelection = this.connector
				.prepareStatement("SELECT id, feature_id, cluster_id FROM related_features where feature_id = ? and cluster_id != ?");
		this.connector.setAutoCommit(false);
		
	}
	public ArrayList<Integer> getFeatureIDs(int clusterID) throws SQLException {
		
		ArrayList<Integer> featureIDsList = new ArrayList<Integer>();
		
		this.RelatedFeaturesSelection.setInt(1,clusterID);
		ResultSet resultSet = RelatedFeaturesSelection.executeQuery();
		while(resultSet.next())
    	{ 		
			int fID = resultSet.getInt(2);
			featureIDsList.add(fID);
    	}		
		
		return featureIDsList;
	}
	public ArrayList<Integer> getclusterIDs(int clusterID, Integer fID) throws SQLException {
		
		ArrayList<Integer> clusterIDsList = new ArrayList<Integer>();
		
		this.ClustersBelongingToFeaturesSelection.setInt(1,fID);
		this.ClustersBelongingToFeaturesSelection.setInt(2,clusterID);
		ResultSet resultSet = ClustersBelongingToFeaturesSelection.executeQuery();
		while(resultSet.next())
    	{ 		
			int cID = resultSet.getInt(3);
			clusterIDsList.add(cID);
    	}		
		
		return clusterIDsList;
	}
	
	

	
}
