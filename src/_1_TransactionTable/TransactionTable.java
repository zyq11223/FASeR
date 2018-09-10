package _1_TransactionTable;

import java.util.ArrayList;

import db_access_layer.DatabaseAccessLayer;

public class TransactionTable {

	public static void main(String args[]) throws Exception
	{		
		//create a transaction table, output to console
		
		//1.iterate over projects table and then over the method IDs and find a check
		//if method is in cluster table, then get its cluster ID, see if that cluster ID has at least 3 instances
		//(for frequent item) then add it into the transaction table row
		
		DatabaseAccessLayer dbLayer = DatabaseAccessLayer.getInstance();
		dbLayer.initializeConnector();
		//populate api_call_index table
		ArrayList<String> sList = dbLayer.getClusterIDsPerProject();
		dbLayer.closeConnector();
		
		for(String s: sList)
		{
			System.out.println(s);
		}
		
	}
}
