/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryrunner;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * QueryRunner takes a list of Queries that are initialized in it's constructor
 * and provides functions that will call the various functions in the QueryJDBC class 
 * which will enable MYSQL queries to be executed. It also has functions to provide the
 * returned data from the Queries. Currently the eventHandlers in QueryFrame call these
 * functions in order to run the Queries.
 */
public class QueryRunner {

    
	public QueryRunner() {
		this.m_jdbcData = new QueryJDBC();
		m_updateAmount = 0;
		m_queryArray = new ArrayList<>();
		m_error = "";

		

		this.m_projectTeamApplication = "SCOPE VENTURES"; // THIS NEEDS TO CHANGE FOR YOUR APPLICATION

		

		// 1. Get all clients and their contact info
        m_queryArray.add(new QueryData(
                "SELECT client_lname, client_fname, start_date, street, state, city, zip, phone, email FROM client INNER JOIN contact_info ON client.contact_info_id = contact_info.contact_info_id",
                null, null, false, false));

        // 2. Get clients that are like first name string and their contact info
        m_queryArray.add(new QueryData(
                "SELECT client_id, client_lname, client_fname, start_date, street, state, city, zip, phone, email FROM client INNER JOIN contact_info ON client.contact_info_id = contact_info.contact_info_id WHERE client.client_fname LIKE ?",
                new String[] { "Client First Name" }, new boolean[] { true }, false, true));

        // 3. Get client account balance by client id
        m_queryArray
                .add(new QueryData("SELECT client_fname, client_lname, account_balance FROM client WHERE client_id = ?",
                        new String[] { "CLIENT_ID" }, new boolean[] { false }, false, true));

        // 4. insert new contact info
        m_queryArray.add(
                new QueryData("INSERT INTO contact_info (street, city, state, zip, phone, email) values (?,?,?,?,?,?)",
                        new String[] { "street", "city", "state", "zip", "phone", "email" },
                        new boolean[] { false, false, false, false, false }, true, true));

        // 5. Get all advisor's clients info by advisor id
        m_queryArray.add(new QueryData(
                "SELECT client_id, client_lname, client_fname, advisor_lname FROM advisor INNER JOIN client ON advisor.advisor_id = client.advisor_id WHERE advisor.advisor_id = ?",
                new String[] { "Advisor ID" }, new boolean[] { false }, false, true));

        // 6. Get all clients investments by client id
        m_queryArray.add(new QueryData(
                "SELECT client_fname, client_lname,investment_name FROM client INNER JOIN client_investments ON client.client_id = client_investments.client_id INNER JOIN investment ON client_investments.investment_id = investment.investment_id WHERE client_investments.client_id = ?",
                new String[] { "Client ID" }, new boolean[] { false }, false, true));

        // 7. Get investments by investment type search by name
        m_queryArray.add(new QueryData(
                "SELECT investment_id, investment_name, street, state, city, zip, phone, email FROM contact_info INNER JOIN investment ON contact_info.contact_info_id = investment.contact_info_id INNER JOIN investment_type ON investment.investment_type_id = investment_type.investment_type_id WHERE investment_type.name LIKE ?",
                new String[] { "Investment Type" }, new boolean[] { true }, false, true));

        // 8. Insert new share by investment ID
        m_queryArray.add(new QueryData("INSERT INTO shares (price_per_share, investment_id) values (?,?)",
                new String[] { "Share Price", "Investment ID" }, new boolean[] { false, false }, true, true));
        
        // 9. update stock price by investment id
        m_queryArray.add(new QueryData("UPDATE shares SET price_per_share = ? WHERE investment_id=?",
                new String[] { "Share Price", "Investment ID" }, new boolean[] { false, false }, true, true));

        // 10. Get all investments and their share price
        m_queryArray.add(new QueryData(
                "SELECT * FROM shares INNER JOIN investment ON shares.investment_id = investment.investment_id ", null,
                null, false, false));
	}
       

    public int GetTotalQueries()
    {
        return m_queryArray.size();
    }
    
    public int GetParameterAmtForQuery(int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.GetParmAmount();
    }
              
    public String  GetParamText(int queryChoice, int parmnum )
    {
       QueryData e=m_queryArray.get(queryChoice);        
       return e.GetParamText(parmnum); 
    }   

    public String GetQueryText(int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.GetQueryString();        
    }
    
    /**
     * Function will return how many rows were updated as a result
     * of the update query
     * @return Returns how many rows were updated
     */
    
    public int GetUpdateAmount()
    {
        return m_updateAmount;
    }
    
    /**
     * Function will return ALL of the Column Headers from the query
     * @return Returns array of column headers
     */
    public String [] GetQueryHeaders()
    {
        return m_jdbcData.GetHeaders();
    }
    
    /**
     * After the query has been run, all of the data has been captured into
     * a multi-dimensional string array which contains all the row's. For each
     * row it also has all the column data. It is in string format
     * @return multi-dimensional array of String data based on the resultset 
     * from the query
     */
    public String[][] GetQueryData()
    {
        return m_jdbcData.GetData();
    }

    public String GetProjectTeamApplication()
    {
        return m_projectTeamApplication;        
    }
    public boolean  isActionQuery (int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.IsQueryAction();
    }
    
    public boolean isParameterQuery(int queryChoice)
    {
        QueryData e=m_queryArray.get(queryChoice);
        return e.IsQueryParm();
    }
    
     
    public boolean ExecuteQuery(int queryChoice, String [] parms)
    {
        boolean bOK = true;
        QueryData e=m_queryArray.get(queryChoice);        
        bOK = m_jdbcData.ExecuteQuery(e.GetQueryString(), parms, e.GetAllLikeParams());
        return bOK;
    }
    
     public boolean ExecuteUpdate(int queryChoice, String [] parms)
    {
        boolean bOK = true;
        QueryData e=m_queryArray.get(queryChoice);        
        bOK = m_jdbcData.ExecuteUpdate(e.GetQueryString(), parms);
        m_updateAmount = m_jdbcData.GetUpdateCount();
        return bOK;
    }   
    
      
    public boolean Connect(String szHost, String szUser, String szPass, String szDatabase)
    {

        boolean bConnect = m_jdbcData.ConnectToDatabase(szHost, szUser, szPass, szDatabase);
        if (bConnect == false)
            m_error = m_jdbcData.GetError();        
        return bConnect;
    }
    
    public boolean Disconnect()
    {
        // Disconnect the JDBCData Object
        boolean bConnect = m_jdbcData.CloseDatabase();
        if (bConnect == false)
            m_error = m_jdbcData.GetError();
        return true;
    }
    
    public String GetError()
    {
        return m_error;
    }
 
    private QueryJDBC m_jdbcData;
    private String m_error;    
    private String m_projectTeamApplication;
    private ArrayList<QueryData> m_queryArray;  
    private int m_updateAmount;
            
    /**
     * @param args the command line arguments
     */
    
    // Console App will Connect to Database
    // It will run a single select query without Parameters
    // It will display the results
    // It will close the database session
    
    public static void main(String[] args) {
        

        final QueryRunner queryrunner = new QueryRunner();
        
        if (args.length == 0)
        {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {

                    new QueryFrame(queryrunner).setVisible(true);
                }            
            });
        }
        else
        {
            if (args[0].equals("-console") )
            {
             
            	Scanner sc = new Scanner(System.in);
            	System.out.print("Hostname: ");
            	String hostname = sc.nextLine();
            	System.out.print("Username: ");
            	String username = sc.nextLine();
            	System.out.print("Password: ");
            	String password = sc.nextLine();
            	System.out.print("Database: ");
            	String database = sc.nextLine();
            	
            	boolean bCONNECT = queryrunner.Connect(hostname, username, password, database);
            	if(bCONNECT == false) {
            		System.err.println(queryrunner.GetError());
            	} else {
            	int totalQueries = queryrunner.GetTotalQueries();
            	for(int i = 0; i < totalQueries; i++) {
            		if(queryrunner.isParameterQuery(i)) {
            			int totalParameters = queryrunner.GetParameterAmtForQuery(i);
            			String[] parms = new String[totalParameters];
            			for(int j = 0; j < totalParameters; j++) {
            				System.out.print(queryrunner.GetParamText(i, j) + ": ");
            				parms[j] = sc.nextLine();
            			}
            			execute(queryrunner, parms, i);
            		} else {
            			String[] parms = {};
            			execute(queryrunner, parms, i);
            		}
            	}
            	}
            	boolean bDISCONNECT = queryrunner.Disconnect();
            	if(bDISCONNECT == false) {
            		System.err.println(queryrunner.GetError());
            	}
            	sc.close();
            }
            else
            {
               System.out.println("usage: you must use -console as your argument to get non-gui functionality. If you leave it out it will be GUI");
            }
        }

    }  
    
    /*
     *  Execute the query depend on its type (regular or action)
     *  print error if execute failed
     */
    private static void execute(QueryRunner queryrunner, String[] parms, int i) {
    	if(queryrunner.isActionQuery(i)) {
			boolean bOK = queryrunner.ExecuteUpdate(i, parms);
			if(bOK) {
				System.out.println("Total amount of affected rows is " + queryrunner.GetUpdateAmount());
			} else {
				System.err.println(queryrunner.GetError());
			}
		} else {
			boolean bOK = queryrunner.ExecuteQuery(i, parms);
			if(bOK) {
				printResult(queryrunner);
			} else {
				System.err.println(queryrunner.GetError());
			}			
		}
    }
    
    /*
     *  Print the data in table form
     */
    private static void printResult(QueryRunner queryrunner) {
    	String[] header = queryrunner.GetQueryHeaders();
		String[][] results = queryrunner.GetQueryData();
		for(String str : header) {
			System.out.print(str + "\t");
		}
		System.out.println();
		for( String[] rows: results) {
			for(String str : rows) {
				System.out.print(str);
				System.out.print("\t");
			}
			System.out.println();
		}
    }
}
