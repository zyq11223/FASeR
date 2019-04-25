package db_access_layer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import DataObjects.Method;

public class EvaluationDAL {

	public static final EvaluationDAL SINGLETON = new EvaluationDAL();
	private Connection connector;

	private PreparedStatement APIUsagesSelection;
	private PreparedStatement MethodSelection;
	private PreparedStatement MethodSelectionByProject;

	public static EvaluationDAL getInstance() {
		return EvaluationDAL.SINGLETON;
	}

	public void initializeHeldoutDatabaseConnector() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		this.connector = DriverManager
				.getConnection(Utilities.Constants.HELDOUT_PROJECT_DATABASE);
		this.APIUsagesSelection = this.connector
				.prepareStatement("select distinct * FROM ( SELECT  api_name, api_usage FROM api_call where api_call.api_name NOT in ('Log','Intent','Toast')) a   ");
		this.connector.setAutoCommit(false);
	}
	
	public void initializeFoldDatabaseConnector() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		this.connector = DriverManager
				.getConnection(Utilities.Constants.DATABASE);
		 this.MethodSelection = this.connector
					.prepareStatement("SELECT cluster.clusterID, method.id, method.name,method.from_line_num, method.to_line_num, file.file_name, file.project_id FROM `cluster` inner join method on method.id = cluster.methodID inner join file on file.id=method.file_id where clusterID = ? ");
		 this.MethodSelectionByProject = this.connector
					.prepareStatement("SELECT cluster.clusterID, method.id, method.name,method.from_line_num, method.to_line_num, file.file_name, file.project_id FROM `cluster` inner join method on method.id = cluster.methodID inner join file on file.id=method.file_id where clusterID = ? and project_id = ?");
	         
		this.APIUsagesSelection = this.connector
				.prepareStatement("select distinct * FROM (SELECT api_name, api_usage FROM api_call where host_method_id = ? and api_call.api_name NOT in ('Log','Intent','Toast')) a ");
			this.connector.setAutoCommit(false);
	}
	
	 public void closeConnector() throws SQLException{
	    	this.connector.close();
	 }
	 
	 public ArrayList<String> getAPIUsages() throws SQLException {
		 
		 ArrayList<String> apiUsagesList = new ArrayList<String>();
		 ResultSet resultSet = APIUsagesSelection.executeQuery();
		 while(resultSet.next())
		 { 	

			 String name = resultSet.getString(1);			
			 String usage = resultSet.getString(2);
			 apiUsagesList.add(name + "." + usage);
		 }		
		 
		 return apiUsagesList;
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
				
				
	    	}		
			//MethodSelection.close();
			return m;
		}
	 
	 public Method getMethodFromProject(Integer clusterID, int projectID) throws SQLException {
			
			Method m = new Method();
			
			MethodSelectionByProject.setInt(1,clusterID);
			MethodSelectionByProject.setInt(2,projectID);
			ResultSet resultSet = MethodSelectionByProject.executeQuery();
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
			
			this.APIUsagesSelection.setInt(1,m.id);
			ResultSet resultSet = APIUsagesSelection.executeQuery();
			while(resultSet.next())
	    	{ 	
				
				String name = resultSet.getString(1);			
				String usage = resultSet.getString(2);
				
				
				apiCallsList.add(name + "." + usage);
	    	}		
			//APICallsSelection.close();
			return apiCallsList;
		}
	 


}
