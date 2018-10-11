package DataObjects;

public class Method {
	public int id;
	public String name;	
	public int from_line_num;
	public int to_line_num;
	public String file_name;
	public int projectID;

		
	public Method()
	{}

	
	public Method(int Id, String Name, int startLineNo,int endLineNo, String file_name, int projectID)
	{
		this.id = Id;
		this.name = Name;		
		this.from_line_num = startLineNo;		
		this.to_line_num =endLineNo;
		this.file_name = file_name;
		this.projectID = projectID;
	}
}
