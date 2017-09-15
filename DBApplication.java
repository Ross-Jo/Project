import java.sql.*;
import java.util.*;
import java.io.*;
import org.postgresql.copy.*;
import org.postgresql.core.*;

public class DBApplication {
	static String DB_DRIVER = "org.postgresql.Driver";
	static String DB_CONNECTION_URL = "jdbc:postgresql://";
	static String IP = null; // connection.txt에서 주어지는 IP 정보
	static String DB_NAME = null; // connection.txt에서 주어지는 DB 이름
	static String SCHEMA_NAME = null; // connection.txt에서 주어지는 스키마 이름
	static String DB_USER = null; // connection.txt에서 주어지는 	아이디
	static String DB_PASSWORD = null; // connection.txt에서 주어지는 	비밀번호
	
	static String TABLE_NAME = null; // test.txt에서 주어지는 테이블의 이름이 저장되는 변수
	static String TABLE_COLUMN_NAME = null; // 칼럼의 이름을 임시로 저장하는 변수
	static String TABLE_COLUMN_TYPE = null; // 칼럼의 타입을 임시로 저장하는 변수
	static Vector<String> TABLE_COLUMN_NAME_ARRAY = null; //  test.txt에서 주어지는 칼럼들의 이름을 저장하는 벡터
	static Vector<String> TABLE_COLUMN_TYPE_ARRAY = null; //  test.txt에서 주어지는 칼럼들의 타입을 저장하는 벡터
	static Vector<String> TABLE_PK_CONSTRAINT = null; //  test.txt에서 주어지는 테이블의 PK 제약조건이 걸리는 칼럼을 저장하는 벡터
	static Vector<String> TABLE_NOT_NULL_CONSTRAINT = null; //  test.txt에서 주어지는  테이블의 NOT NULL 제약조건이 걸리는 칼럼을 저장하는 벡터
	
	public static void main(String[] args){
		
		Connection conn = null; // Connection 클래스 선언
		Statement stmt = null; // Statement 클래스 선언(스트링으로 작성한 sql 쿼리문을 실행하는데 필요)
		String line = null; // 파일을 읽어들이는데 사용할 스트링변수 선언
		StringTokenizer st = null; // 읽어들인 스트링을 자르고 편집하기 위한 스트링토크나이저 변수 선언
		
		try{
		//*************************************************************************//
		BufferedReader in_connection = new BufferedReader(new FileReader("connection.txt"));

		/*connection.txt의 IP를 읽어들이는 부분*/
		line = in_connection.readLine();
		st = new StringTokenizer(line,":");
		for(int i=0;i<2;i++) IP = st.nextToken();
		IP = IP.trim();
		//System.out.println(IP);
		
		/*connection.txt의 DB이름을 읽어들이는 부분*/
		line = in_connection.readLine();
		st = new StringTokenizer(line,":");
		for(int i=0;i<2;i++) DB_NAME = st.nextToken();
		DB_NAME = DB_NAME.trim();
		//System.out.println(DB_NAME);
		
		/*connection.txt의 스키마이름을 읽어들이는 부분*/
		line = in_connection.readLine();
		st = new StringTokenizer(line,":");
		for(int i=0;i<2;i++) SCHEMA_NAME = st.nextToken();
		SCHEMA_NAME = SCHEMA_NAME.trim();
		//System.out.println(SCHEMA_NAME);
		
		/*connection.txt의 사용자이름을 읽어들이는 부분*/
		line = in_connection.readLine();	
		st = new StringTokenizer(line,": ");
		for(int i=0;i<2;i++) DB_USER = st.nextToken();
		DB_USER = DB_USER.trim();
		//System.out.println(DB_USER);
		
		/*connection.txt의 비밀번호를 읽어들이는 부분*/
		line = in_connection.readLine();
		st = new StringTokenizer(line,": ");
		for(int i=0;i<2;i++) DB_PASSWORD = st.nextToken();
		DB_PASSWORD = DB_PASSWORD.trim();
		//System.out.println(DB_PASSWORD);
		
		/*DB커넥션을 위한 String을 완성*/
		DB_CONNECTION_URL = DB_CONNECTION_URL+IP+"/"+DB_NAME;
		//System.out.println(DB_CONNECTION_URL);
		
		Class.forName(DB_DRIVER); // JDBC 프로그래밍 1단계(PostgreSQL서버의 JDBC 드라이버 로드) : 데이터베이스와의 연결, JDBC 드라이버의 로드 
		                                        // JDBC 드라이버 클래스를 로드하여 드라이버 인스턴스 생성 후, DriverManager에 등록
		
		conn = DriverManager.getConnection(DB_CONNECTION_URL, DB_USER, DB_PASSWORD); // JDBC프로그래밍 2단계(자바 응용프로그램과 JDBC의 연결) : DriverManager는 자바 응용프로그램을 JDBC드라이버에 연결시켜주는 클래스
                                        //postgreSQL URL 형식, username, password 입력
		conn.setAutoCommit(true); // autocommit을 true로 설정(과제 3번의 요구사항)
		//System.out.println("DB Connection complete");
		
		in_connection.close(); // connection.txt 파일을 읽어들이는 부분은 끝났기 때문에 연결을 끊어준다.
		//*************************************************************************//
		
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE); // sql문 실행을 위해서 connection 객체와 연결된 구문을 지정
		
		//*************************************************************************//
		MainMenu(stmt, TABLE_COLUMN_NAME_ARRAY, TABLE_COLUMN_TYPE_ARRAY, conn);
		}
		catch(IOException e){
			System.out.println("File Not Found");
		}
		catch(ClassNotFoundException e){
			System.out.println("Class Not Found");
		}
		catch(SQLException e){
			System.out.println("Wrong SQL Statement / Relation specified in the file you typed is already exist");
			//e.printStackTrace();
		}
	}

	// 수정 소요 : 각 함수별로 Vector<String> TABLE_COLUMN_NAME_ARRAY, Vector<String> TABLE_COLUMN_TYPE_ARRAY 넘기는 인자값들 수정. 의미가 없이 넘기는 것들이 있음
	public static void MainMenu(Statement stmt, Vector<String> TABLE_COLUMN_NAME_ARRAY, Vector<String> TABLE_COLUMN_TYPE_ARRAY, Connection conn) throws SQLException {
		Scanner sc = new Scanner(System.in);
		boolean mainMenuRun = true;
		int inputNum = 0;

		while (mainMenuRun) {
			System.out.print(
					"Please input the instruction number (1: Import from CSV, 2: Export to CSV, 3: Manipulate Data, 4: Exit) : ");
			inputNum = sc.nextInt();

			switch (inputNum) {
			case 1:
				importFromCSV(stmt, TABLE_COLUMN_NAME_ARRAY);
				break;
			case 2:
				exportToCSV(stmt, conn);
				break;
			case 3:
				manipulateDataMenu(stmt);
				break;
			case 4:
				mainMenuRun = false;
				break;
			}
		}
	}

	public static void importFromCSV(Statement stmt, Vector<String> TABLE_COLUMN_NAME_ARRAY) throws SQLException {
		
		String line = null; // 파일을 읽어들이는데 사용할 스트링변수 선언
		StringTokenizer st = null; // 읽어들인 스트링을 자르고 편집하기 위한 스트링토크나이저 변수 선언
		
		Scanner sc = new Scanner(System.in);

		String tableFileName = null;
		String csvFileName = null;
		
		String csvColumNameLine = null;
		String csvTempLine = null;
		
		int linePtr = 1;
		int successCount = 0;
		int failureCount = 0;
		
		ArrayList<Integer> failureIntegerList = new ArrayList<Integer>(); // 오류 발생시 linePtr 값을 저장
		ArrayList<String> failureStringList = new ArrayList<String>(); // 오류 발생 시 해당 tuple을 통째로 옮기기

		System.out.println("[Import from CSV]");
		
		// 1. test.txt 파일을 받고 Table 포맷 생성
		
		//*************************************************************************//
		/*여기서 부터는 test.txt을 읽어들이는 구간*/
		try{
		System.out.print("Please specify the filename for table description : ");
		tableFileName = sc.nextLine();
		
		BufferedReader in_tableReader = new BufferedReader(new FileReader(tableFileName));
		
		/* test.txt의 테이블 이름을 읽어들이는 부분*/
		line = in_tableReader.readLine();
		st = new StringTokenizer(line,":");
		for(int i=0;i<2;i++) TABLE_NAME = st.nextToken();
		TABLE_NAME = TABLE_NAME.trim();
		//System.out.println(TABLE_NAME);
		
		/* test.txt의 테이블 정보(컬럼의 이름 및 타입)를 읽어들이는 부분*/
		line = in_tableReader.readLine();
		st = new StringTokenizer(line,":");
		TABLE_COLUMN_NAME_ARRAY = new Vector<String>(); TABLE_COLUMN_TYPE_ARRAY = new Vector<String>(); // 테이블내 칼럼의 이름과 타입을 담아두기 위한 벡터 생성
		while(!st.nextToken().substring(0,2).equals("PK")){ // Column으로 시작하는 문장이 나올 때
				for(int i=0;i<1;i++) TABLE_COLUMN_NAME = st.nextToken();
				TABLE_COLUMN_NAME = TABLE_COLUMN_NAME.trim();
				TABLE_COLUMN_NAME_ARRAY.addElement(TABLE_COLUMN_NAME); // 컬럼 이름이 먼저 나오니까 먼저 이름을 담고
				
				line = in_tableReader.readLine();
				st = new StringTokenizer(line,":");
				for(int i=0;i<2;i++) TABLE_COLUMN_TYPE = st.nextToken(); // 다음으로 컬럼 타입을 담아줌
				TABLE_COLUMN_TYPE = TABLE_COLUMN_TYPE.trim();
				TABLE_COLUMN_TYPE_ARRAY.addElement(TABLE_COLUMN_TYPE);
				
				line = in_tableReader.readLine(); // 그리고 계속 줄을 넘겨주며
				st = new StringTokenizer(line,":"); // 해당 라인에서의 스트링토크나이저를 생성
		}
		//System.out.println("통과함");
		
		/* test.txt의 테이블 정보(PK 제약조건)을 읽어들이는 부분*/
		st = new StringTokenizer(line,":,");
		TABLE_PK_CONSTRAINT = new Vector<String>();
		
		int num = st.countTokens();
		for(int i=0;i<num;i++){
			String tmp = st.nextToken();
			tmp = tmp.trim();
			if(i>=1){
				TABLE_PK_CONSTRAINT.addElement(tmp);
			}
		}
		
		/* test.txt의 테이블 정보(Not null 제약조건)을 읽어들이는 부분*/
		line = in_tableReader.readLine();
		st = new StringTokenizer(line,":,");
		num = st.countTokens();
		TABLE_NOT_NULL_CONSTRAINT = new Vector<String>();
		for(int i=0;i<num;i++){
			String tmp = st.nextToken();
			tmp = tmp.trim();
			if(i>=1){
				TABLE_NOT_NULL_CONSTRAINT.addElement(tmp);
			}
		}
		
		in_tableReader.close(); // test.txt 파일을 읽어들이는 부분은 끝났기 때문에 연결을 끊어준다.

		//*************************************************************************//

		/*
		 * Table 생성 작업을 여기에 기록
		 */
	
		/*앞서 받아들인 정보로 테이블을 생성하는 부분*/
		String CreateTableSQL_INPUT = "CREATE TABLE " + "\"" + SCHEMA_NAME + "\"" + "." + "\"" + TABLE_NAME + "\"" + " (";
				
				for(int i=0;i<TABLE_COLUMN_NAME_ARRAY.size();i++){
					int check = 0;
					CreateTableSQL_INPUT = CreateTableSQL_INPUT +"\""+TABLE_COLUMN_NAME_ARRAY.elementAt(i)+"\""+" "+ TABLE_COLUMN_TYPE_ARRAY.elementAt(i);
					for(int j=0;j<TABLE_NOT_NULL_CONSTRAINT.size();j++){
							if(TABLE_COLUMN_NAME_ARRAY.elementAt(i).equals(TABLE_NOT_NULL_CONSTRAINT.elementAt(j))) { // 추후 수정소요 필요 : 컬럼 이름이 번호가 아닐경우 혹은 컬럼 이름이 이상할 경우
								CreateTableSQL_INPUT = CreateTableSQL_INPUT + " not null, ";
								check++;
							}
					} 	
					if(check==0) CreateTableSQL_INPUT = CreateTableSQL_INPUT + ", ";
				}					
				
				CreateTableSQL_INPUT = CreateTableSQL_INPUT + "primary key (";
				
				for(int i=0;i<TABLE_PK_CONSTRAINT.size();i++){
					CreateTableSQL_INPUT = CreateTableSQL_INPUT +"\""+TABLE_PK_CONSTRAINT.elementAt(i)+"\"";
					if(i!=TABLE_PK_CONSTRAINT.size()-1) {
						CreateTableSQL_INPUT = CreateTableSQL_INPUT + ", ";
					}
					if(i==TABLE_PK_CONSTRAINT.size()-1) {
						CreateTableSQL_INPUT = CreateTableSQL_INPUT + ")";
					}
				}
				
				CreateTableSQL_INPUT = CreateTableSQL_INPUT + ")";
				//System.out.println(CreateTableSQL_INPUT);
				
				stmt.executeUpdate(CreateTableSQL_INPUT); // test.txt파일을 읽어들여 받아들인 테이블 정보를 쿼리로 날려 DB에서 테이블 생성

		System.out.println("Table is newly created as described in the file.");

		// 2. CSV 파일 받아서 실제값 넣기
		System.out.print("Please specify the CSV filename : ");
		csvFileName = sc.nextLine();
		
		InputStream in_csvTableReader_tmp = new FileInputStream(csvFileName);
		BufferedReader in_csvTableReader = new BufferedReader(new InputStreamReader(in_csvTableReader_tmp, "UTF-8")); // UTF8로 형식지정
		
		/*생성된 테이블에 CSV파일의 튜플 삽입*/
		csvColumNameLine = in_csvTableReader.readLine(); // csv파일의 첫줄에서 튜플 명을 읽어들임
		
		// 추가 수정 부분 *************************
		String str = "";
		StringTokenizer st1 = new StringTokenizer(csvColumNameLine, ", ");
		ArrayList<String> lst = new ArrayList<>();
		
		while(st1.hasMoreTokens()) {
			lst.add(st1.nextToken());
		}
		
		for(int i = 0; i < lst.size() - 1; i++) {
			str = str + "\"" + lst.get(i) + "\", ";
		}
		str = str + "\"" + lst.get(lst.size() - 1) + "\"";
		
		// 여기까지 *************************
		
		StringTokenizer st_csv = new StringTokenizer(csvColumNameLine, ",");
		int tmp_length = st_csv.countTokens();
		
		if(TABLE_COLUMN_NAME_ARRAY.size()==tmp_length){
			/*
			 * <예외처리>
			 * if(table의 column 개수 != csv 파일의 column 개수)
			 * System.out.println("Data import failure. (The number of columns does not match between the table description and the CSV file.)");
			 * 
			 * 판단 방법은 
			 * 1) 처음에 table을 생성했을 때 tableColumn 개수를 미리 저장
			 * 2) csv 파일의 맨 첫 번째 줄에서 tuple 개수 저장
			 * 3) 이 두 개를 먼저 비교해서 일치하지 않을 시 에러 메시지 출력 후 곧바로 return;을 시켜 함수를 종료시키기
			 */
			
			while((csvTempLine = in_csvTableReader.readLine())!=null){ // 튜플을 한줄한줄 삽입한다. 모든 라인을 읽어들일때까지
				try{
					st_csv = new StringTokenizer(csvTempLine, ",");
					String csvTempModifiedLine = "";
					for(int i=0;i<tmp_length;i++){
						csvTempModifiedLine += "'"+st_csv.nextToken()+"'"; // 삽입할 문장의 모든 input요소에 따옴표를 붙여준다
						if(i!=tmp_length-1) csvTempModifiedLine += ",";
					}
					// 테스트 용도
					// System.out.println("insert into "+"\""+SCHEMA_NAME+"\""+"."+"\""+TABLE_NAME+"\""+" ("+str+")"+" values "+"("+csvTempModifiedLine+")");
					if(stmt.executeUpdate("insert into "+"\""+SCHEMA_NAME+"\""+"."+"\""+TABLE_NAME+"\""+" ("+str+")"+" values "+"("+csvTempModifiedLine+")")==1){
						successCount++;
					}
					linePtr++; // Ptr변수가 가리키는 라인을 증가시킨다.
				}
				catch(Exception e){
					failureCount++; /// 에러가 발생하여 실행이 되지 않은 경우 실패 횟수를 증가시킨다.
					failureIntegerList.add(linePtr++); // 실패한 줄이 어디인지 저장한다.
					failureStringList.add(csvTempLine); // 실패한 줄의 실패 튜플이 어떤 내용인지 저장한다.
				}
			}
		}
		else{
			System.out.println("Data import failure. (The number of columns does not match between the table description and the CSV file.)");
			System.out.println();
			in_csvTableReader.close(); // 앞서 열었던 in_csvTableReader를 닫아줌
			return;
		}
		/*
		 * 1) csv 파일 읽어내기 2) PK, Not null 조건 만족 못할 시 failure 리턴 
		 * successCount, failureCount 변수 설정 후 failure 발생 시 ++시키기
		 */
		System.out.println("Data import completed. (Insertion Success : " + successCount + ", Insertion Failure : " + failureCount + ")");

		if (failureCount >= 1) {
			/*
			 * failure가 0이면 그냥 패스, 만약 failureCount >= 1이면 Failed Tuple 표시하기
			 * ArrayList에 오류나는 라인을 전체다
			 */
			for (int i = 0; i < failureCount; i++) {
				System.out.println("Failed tuple : " + failureIntegerList.get(i) + " line in CSV - " + failureStringList.get(i));
			}
		}
		System.out.println();
		
		in_csvTableReader.close(); // 앞서 열었던 in_csvTableReader를 닫아줌
		} // try문의 괄호닫기
		catch(IOException e){
			System.out.println("파일을 찾을 수 없습니다.");
		}
	}

	public static void exportToCSV(Statement stmt, Connection conn) throws SQLException {
		try{
		Scanner sc = new Scanner(System.in);
		
		String tableName = null;
		String csvFileName = null;
		
		System.out.println("[Export to CSV]");
		
		// 1. Table 이름 받기
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		
		/*
		 * 해당 Table로 접근해서 데이터 가져오기
		 */
		
		CopyManager cm = new CopyManager((BaseConnection)conn); // 카피매니저를 생성함

		// 2. 해당 자료를 Export 시킬 csv 파일 이름 설정
		System.out.print("Please specify the CSV filename : ");
		csvFileName = sc.nextLine();
		
		/*
		 * 해당 csv 파일로 내보내기
		 * <주의사항>
		 * 1) 동일한 파일이 이미 있을 시 덮어쓰기(이게 자동으로 되는 건지, 우리가 수동으로 처리해야 하는지 구글링)
		 * 2) UTF-8으로 인코딩(이것 역시 자동으로 되는 건지, 우리가 수동으로 처리해야 하는지 구글링)
		 */
		OutputStream out_tmp = new FileOutputStream(csvFileName);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(out_tmp,"UTF-8")); // scv파일을 내보낼 파일 아웃풋 스트림을 생성, UTF8로 형식지정
		
		String colum_tmp = ""; // CSV로의 출력을 위해 칼럼이름들을 덧붙이기 위한 임시 변수
		
		// 추가 수정 부분 *************************
		String column_tmp2 = "";
		
		DatabaseMetaData metaData = conn.getMetaData(); // 칼럼 이름들을 읽어오는데 필요한 메타 데이터를 생성함
		ResultSet rs = metaData.getColumns(null, null, tableName, null); // 메타 데이터 call은 case sensitive
		
		
		// 추가 수정 부분 *************************
		ArrayList<String> firstRowList = new ArrayList<>();
		
		while(rs.next()){
			String str = rs.getString("COLUMN_NAME");
			colum_tmp += "\"" + str  + "\""; // 쿼리문 작성을 위하여 칼럼 명들을 합쳐 string으로 만듦
			colum_tmp += ", "; // 칼럼이름 마다 쉼표로 구분
			
			firstRowList.add(str);			
		}
		//System.out.println(colum_tmp);
		int tmp_length = 0; 
		if(colum_tmp.length()!=0) tmp_length = colum_tmp.length();
		else {
			tmp_length = 2;
			System.out.println("There's no such table");
			System.out.println();
			out.close();
			return;
		}
		colum_tmp = colum_tmp.substring(0, tmp_length-2); // 마지막 ", "는 필요 없으니 잘라냄
		
		
		for (int i = 0; i < firstRowList.size() - 1; i++) {
			column_tmp2 = column_tmp2 + firstRowList.get(i) +",";
		}
		
		column_tmp2 = column_tmp2 + firstRowList.get(firstRowList.size() - 1);
		// System.out.println(column_tmp2);
		// 컬럼명들 넣기
		out.write(column_tmp2 + "\n");
		// 여기까지 *************************************
		
		//System.out.println("COPY \"" + SCHEMA_NAME + "\"." +"\""+tableName+"\"" + " ("+colum_tmp+") "+"TO STDOUT WITH DELIMITER ','");
		cm.copyOut("COPY " +"\""+tableName+"\"" + " ("+colum_tmp+") "+"TO STDOUT WITH DELIMITER ','", out);
		
		out.close();
		
		System.out.println("Data export completed.");
		System.out.println();
		}
		
		catch(IOException e){
			//e.printStackTrace();
			System.out.println("File export error");
			System.out.println();
		}
		catch(Exception e){
			//e.printStackTrace();
			System.out.println("File export error");
			System.out.println();
		}
	}
	
	 public static void manipulateDataMenu(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		boolean manipulateMenuRun = true;
		int inputNum = 0;
		
		while (manipulateMenuRun) {
			System.out.print(
					"Please input the instruction number (1: Show Tables, 2: Describe Table, 3: Select, 4: Insert, 5: Delete, 6: Update, 7: Drop Table, 8: Back to main) : ");
			inputNum = sc.nextInt();

			switch (inputNum) {
			case 1:
				showTable(stmt);
				break;
			case 2:
				describeTable(stmt);
				break;
			case 3:
				selectOperation(stmt);
				break;
			case 4:
				insertOperation(stmt);
				break;	
			case 5:
				deleteOperation(stmt);
				break;
			case 6:
				updateOperation(stmt);
				break;
			case 7:
				dropTable(stmt);
				break;
			case 8:
				manipulateMenuRun = false;
				break;
			default:
				System.out.println("Input Error! Input between 1 to 8!");
			}
		}
		
	}
	 
	public static void showTable(Statement stmt) throws SQLException {
		System.out.println("=======");
		System.out.println("Table List");
		System.out.println("=======");
		
		//System.out.println("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '"+SCHEMA_NAME+"'");
		ResultSet rs = stmt.executeQuery("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '"+SCHEMA_NAME+"'"); // 유의 필요 - 여기는 큰따옴표 붙이면 안됨
		
		while(rs.next()){
			System.out.println(rs.getString("TABLE_NAME")); // Connection 시 입력한 스키마 정보를 기반으로, 테이블 이름 출력
		}
		/*
		 * connection.txt에서 입력한 SCHEMA_NAME(여기서는 public)을 기반으로 자료를 가져오기
		 * metadata 추출 방법 링크 확인하기(https://www.postgresql.org/docs/9.6/static/infoschema-tables.html)
		 */
		System.out.println();
	}
	
	public static void describeTable(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		String tableName = null;
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		
		System.out.println("==========================================================");
		System.out.println("Column Name | Data Type | Character Maximum Length(or Numeric Precision and Scale)");
		System.out.println("==========================================================");
		
		ResultSet rs = stmt.executeQuery("select TABLE_NAME, COLUMN_NAME, DATA_TYPE, NUMERIC_PRECISION, NUMERIC_SCALE, CHARACTER_MAXIMUM_LENGTH from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = '"+SCHEMA_NAME+"'"+" and TABLE_NAME = '"+tableName +"'"); // 유의필요 - 여기도 스키마에 큰따옴표 금지
		while(rs.next()){
			System.out.print(rs.getString("COLUMN_NAME")+", "+rs.getString("DATA_TYPE")+", "); // Connection 시 입력한 스키마 정보를 기반으로, 테이블 이름 출력
			if(rs.getString("DATA_TYPE").equals("integer")||rs.getString("DATA_TYPE").equals("numeric")){
				System.out.println("("+rs.getString("NUMERIC_PRECISION")+", "+rs.getString("NUMERIC_SCALE")+")");
			}
			else if(rs.getString("DATA_TYPE").equals("character")||rs.getString("DATA_TYPE").equals("character varying")){
				System.out.println(rs.getString("CHARACTER_MAXIMUM_LENGTH"));
			}
			else{
				System.out.println();
			}
		}
		System.out.println();
		/*
		 * column명, data type, 세부정보 출력
		 */
	}
	
	public static void selectOperation(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		String tableName = ""; // 테이블 이름
		String selectColName = ""; // select 절의 column들
		String whereColName = ""; // where 절의 column들
		String orderColName = ""; // order by 절의 column들
		String orderCriteria = ""; // desc, asc의 정렬 기준
		
		// where절의 쓰게 될 시 넣어야 하는 값
		int conditionOperatorNum = 0; // 입력을 받을 때는 숫자로 받음
		String conditionOperator = ""; // 입력받은 번호를 기반으로 지정
		String conditionValue = ""; // 비교값
		int logicalOperatorNum = 0;
		String logicalOperator = ""; // AND, OR
		
		String whereStatement = ""; // where 조건을 추가할 때마다 계속 갱신된다
		String whereSQLStatement = "";
		
		String orderByStatement = "";
				
		//*******************************************************************************//
		// [입력 Part]
		// 1. 테이블명 받기 
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		// 테이블 이름에  "" 씌우기
		tableName = "\"" + SCHEMA_NAME + "\"" + "." +"\"" + tableName + "\"";
				
		// 2. select 절의 column명 받기
		System.out.print("Please specify columns which you want to retrieve (ALL : *) : ");
		selectColName = sc.nextLine();
		
		// 3. where 절의 column명 받기
		boolean conditionRun = true;
		
		sc = new Scanner(System.in);
		System.out.print("Please specify the column which you want to make condition (Press enter : skip) : ");
		whereColName = sc.nextLine();
		
		// 그냥 엔터 했을 시는 패스, 입력 받았을 시에는 3.finish 입력 전까지는 while문 돌리기
		if (!whereColName.equals("")) {
			// 1. 연산자 입력 받기
			System.out.print("Please specify the condition (1: =, 2: >, 3: < , 4: >=, 5: <=, 6: !=, 7: LIKE) : ");
			conditionOperatorNum = sc.nextInt();
			// 연산자를 번호로 받았기에 String으로 바꾸기
			switch (conditionOperatorNum) {
			case 1:
				conditionOperator = "=";
				break;
			case 2:
				conditionOperator = ">";
				break;
			case 3:
				conditionOperator = "<";
				break;
			case 4:
				conditionOperator = ">=";
				break;
			case 5:
				conditionOperator = "<=";
				break;
			case 6:
				conditionOperator = "!=";
				break;
			case 7:
				conditionOperator = "LIKE";
				break;
			}
			
			// 2. 인터페이스에 표시될 문장 만들기
			// "COURSE_ID = "까지만 들어감
			whereStatement = whereStatement + whereColName + " " + conditionOperator + " " ;
			// 실제 SQL문에는 컬럼명에 ""를 씌우기
			whereSQLStatement = whereSQLStatement + "\"" + whereColName + "\" " + conditionOperator + " " ;
			
			// 3. 비교값 입력, preparedStatement에 들어가기에 String으로 받아도 된다. 예외처리는 필요없다고 가정함
			System.out.print("Please specify the condition value (" + whereStatement + "?) : ");
			sc = new Scanner(System.in); // flush 작업
			conditionValue = sc.nextLine();
			
			// 4. And, Or, finish 결정. 이 역시 Switch문으로 처리
			System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
			logicalOperatorNum = sc.nextInt();
			switch (logicalOperatorNum) {
			case 1:
				logicalOperator = "and";
				conditionRun = true;
				break;
			case 2:
				logicalOperator = "or";
				conditionRun = true;
				break;
			case 3:
				logicalOperator = "";
				conditionRun = false;
				break;
			}
			
			// 5. whereStatement 재수정
			// "COURSE_ID = 100 or " 까지만 생성
			whereStatement = whereStatement + conditionValue + " " + logicalOperator + " ";
			whereSQLStatement = whereSQLStatement + "'" + conditionValue + "' " + logicalOperator + " ";
			
			while(conditionRun) {
				sc = new Scanner(System.in);
				System.out.print("Please specify the column which you want to make condition : ");
				whereColName = sc.nextLine();
				
				// 그냥 엔터 했을 시는 패스, 입력 받았을 시에는 3.finish 입력 전까지는 while문 돌리기
				if (!whereColName.equals("")) {
					// 1. 연산자 입력 받기
					System.out.print("Please specify the condition (1: =, 2: >, 3: < , 4: >=, 5: <=, 6: !=, 7: LIKE) : ");
					conditionOperatorNum = sc.nextInt();
					// 연산자를 번호로 받았기에 String으로 바꾸기
					switch (conditionOperatorNum) {
					case 1:
						conditionOperator = "=";
						break;
					case 2:
						conditionOperator = ">";
						break;
					case 3:
						conditionOperator = "<";
						break;
					case 4:
						conditionOperator = ">=";
						break;
					case 5:
						conditionOperator = "<=";
						break;
					case 6:
						conditionOperator = "!=";
						break;
					case 7:
						conditionOperator = "LIKE";
						break;
					}
					
					// 2. 인터페이스에 표시될 문장 만들기
					// "COURSE_ID = "까지만 들어감
					whereStatement = whereStatement + whereColName + " " + conditionOperator + " " ;
					// 실제 SQL문에는 컬럼명에 ""를 씌우기
					whereSQLStatement = whereSQLStatement + "\"" + whereColName + "\" " + conditionOperator + " " ;
					
					// 3. 비교값 입력, preparedStatement에 들어가기에 String으로 받아도 된다. 예외처리는 필요없다고 가정함
					System.out.print("Please specify the condition value (" + whereStatement + "?) : ");
					sc = new Scanner(System.in); // flush 작업
					conditionValue = sc.nextLine();
					
					// 4. And, Or, finish 결정. 이 역시 Switch문으로 처리
					System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
					logicalOperatorNum = sc.nextInt();
					switch (logicalOperatorNum) {
					case 1:
						logicalOperator = "and";
						conditionRun = true;
						break;
					case 2:
						logicalOperator = "or";
						conditionRun = true;
						break;
					case 3:
						logicalOperator = "";
						conditionRun = false;
						break;
					}
					
					// 5. whereStatement 재수정
					// "COURSE_ID = 100 or " 까지만 생성
					whereStatement = whereStatement + conditionValue + " " + logicalOperator + " ";
					whereSQLStatement = whereSQLStatement + "'" + conditionValue + "' " + logicalOperator + " ";
			
				}
			}	
		}
		
		whereSQLStatement = whereSQLStatement.trim();
		
		// sc 변수를 flush하기
		sc = new Scanner(System.in);
		
		ArrayList<String> orderColNameArrList = new ArrayList<>();
		ArrayList<String> orderCriteriaArrList = new ArrayList<>();
		
		// 4. order by 절의 column명 받기
		System.out.print("Please specify the column name for ordering (Press enter : skip) : ");
		orderColName = sc.nextLine();
		
		if (!orderColName.equals("")) {
			StringTokenizer st = new StringTokenizer(orderColName, ", ");
			while (st.hasMoreTokens()) {
				orderColNameArrList.add(st.nextToken());
			}
			
			// desc, asc 받기
			System.out.print("Please specify the sorting criteria (Press enter : skip) : ");
			orderCriteria = sc.nextLine();
			if (!orderCriteria.equals("")) {
				/*
				 * <여기 매우 주의!>
				 * ASCEND는 ASC, DESCEND는 DESC로 변환
				 * ORDER BY "COURSE_ID" ASC, "GRADE" DESC로 해야하기에 콤마를 기준으로 split을 반드시 해야 함
				 */
				st = new StringTokenizer(orderCriteria, ", ");
				while (st.hasMoreTokens()) {
					String str = st.nextToken();
					if (str.equals("ASCEND") || str.equals("ascend")) {
						str = "ASC";
					} else {
						str = "DESC";
					}
					orderCriteriaArrList.add(str);
				}
				
				// orderByStatement 갱신하기
				for (int i = 0; i < orderColNameArrList.size() - 1; i++) {
					orderByStatement = orderByStatement + "\"" + orderColNameArrList.get(i) + "\" " + orderCriteriaArrList.get(i) + ", ";
				}
				orderByStatement = orderByStatement + "\"" + orderColNameArrList.get(orderColNameArrList.size() - 1) + "\" " + orderCriteriaArrList.get(orderColNameArrList.size() - 1);
			} else {
				// orderByStatement 갱신하기
				for (int i = 0; i < orderColNameArrList.size() - 1; i++) {
					orderByStatement = orderByStatement + "\""+ orderColNameArrList.get(i) + "\", ";
				}
				orderByStatement = orderByStatement + "\"" + orderColNameArrList.get(orderColNameArrList.size() - 1) + "\"";
			}
		}
		
		// 테스트 용도
//		System.out.println(tableName);
//		System.out.println(selectColName);
//		System.out.println(whereStatement);
//		System.out.println(orderColName);
//		System.out.println(orderCriteria);
//		System.out.println(whereSQLStatement);
//		System.out.println(orderByStatement);
		
		//************************************************************************************//
		// [출력 Part]
		// 1. 예외처리(잘못된 컬럼명 입력 시 <error detected> 출력하고 return하기
		
		
		// 2. 아마 이 즈음에서 select문 작업을 해야 할듯
		/*
		 * selectedRow의 값을 반드시 저장해놔라
		 * for문으로 돌리면 하나씩 확인하던지, metadata의 정보를 이용하여 확인하던지
		 */
		String SelectSQL = "SELECT ";
		// *가 아닐 시 쪼개서 하나씩 "" 씌우기
		
		ArrayList<String> selectColList = null;
		
		if(!selectColName.equals("*")) {
			selectColList = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(selectColName, ", ");
			while(st.hasMoreTokens()) {
				selectColList.add(st.nextToken());
			}
			for(int i = 0; i < selectColList.size() - 1; i++) {
				SelectSQL = SelectSQL + "\"" + selectColList.get(i) + "\", ";
			}
			SelectSQL = SelectSQL + "\"" + selectColList.get(selectColList.size() - 1) + "\"";
		} else {
			SelectSQL = SelectSQL + "*";
		}
		
		SelectSQL = SelectSQL + " FROM " + tableName;
		
		if(!whereSQLStatement.equals("")){
			SelectSQL = SelectSQL + " WHERE " + whereSQLStatement;
		}
		
		if(!orderByStatement.equals("")){
			SelectSQL = SelectSQL + " ORDER BY " + orderByStatement;
		}
		
		// System.out.println(SelectSQL);
		
		ResultSet rs = null;
		// 예외처리 여기서 작업
		try {
			rs = stmt.executeQuery(SelectSQL);
		} catch(Exception e) {
			System.out.println("<error Detected>");
			System.out.println();
			return;
		}

		// 3. Column 리스트 출력
		String colList = "";

		int colNum = rs.getMetaData().getColumnCount(); // 선택한 칼럼의 갯수를 받아옴
		int colTmpNum = 0;
		for(int i=0;i<colNum;i++){
			colList += rs.getMetaData().getColumnLabel(colTmpNum+1); // 받아온 칼럼 갯수를 이용하여 칼럼의 이름을 출력
			colList += " | "; // delimeter 삽입
			colTmpNum++;
		}
		colList = colList.substring(0, colList.length()-3); // 필요 없는 delimeter 삭제

		/*
		 * 여기에 column들을 |(delimeter)로 구분하며 모두 넣기
		 * * (all) 일 때는 모든 column들을 넣기(맨 처음에 if문으로 체크하기)
		 */
		System.out.println("==========================================================");
		System.out.println(colList);
		System.out.println("==========================================================");
		
		// 4. 각각의 record들을 출력하기
		/* 
		 * JDBC 연결
		 * selectedRow의 값이 매우 중요함
		 * 10개를 읽으면 selectedRow = selectedRow - 10 하기
		 */
	    
		rs.last(); // ResultSet에 달려있는 커서를 맨 마지막으로 보냄
	    int totalRows = rs.getRow(); // Row의 갯수를 읽어오기 위한 부분
	    rs.beforeFirst(); // 다시 커서를 맨 처음으로 보냄
		
		int tmpNumOfRows = 0; colTmpNum = 0; // 열의 위치를 세기 위한 변수를 설정, 임시적으로 칼럼을 조절하기 위한 변수 초기화
		String colName = null;	// 칼럼 이름을 이용하여 Row를 출력하기 위한 임시 변수 사용
	    
		for(int k=0;k<totalRows;k++){ // 모든 row에 대하여
			rs.next(); // 행을 넘기면서 작업을 하는데
			for(int i=0;i<colNum;i++){
			colName = rs.getMetaData().getColumnLabel((colTmpNum++)+1); // 왼쪽부터 오른쪽으로 컬럼을 선택해 나가며
			System.out.print(rs.getString(colName)); // 가리키로 있는 row의 해당 컬럼의 데이터를 출력함
			if(i!=colNum-1) System.out.print(", "); // 만약 마지막 컬럼이 아니라면 계속 쉼표를 붙여줌
			}
			colTmpNum = 0; // 컬럼 조절을 위한 변수 초기화
			tmpNumOfRows++; // row를 하나 읽었으니, 읽을 때마다 row를 증가
			System.out.println();
			if(tmpNumOfRows%10==0&&k!=totalRows-1){ // 10개의 row를 읽으면 enter를 쳐야 다음으로 넘어감
				do{
				sc = new Scanner(System.in);
				System.out.print("<Press enter>");
				}
				while(sc.nextLine().equals("")!=true);
			}
		}
		
		// 5. 총 몇 개의 totalRow 읽었는지 출력
		// 0, 1개 일 때는 row, 2개 이상일 때는 rows로 출력
		if (totalRows == 0 || totalRows == 1) {
			System.out.println("<"+ totalRows +" row selected>");
		} else {
			System.out.println("<"+ totalRows +" rows selected>");
		}
		
		System.out.println();
	}
	
	public static void insertOperation(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		String tableName = null;
		String colList = null;
		String valueList = null;
		
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		// 테이블 이름에  "" 씌우기
		tableName = "\"" + SCHEMA_NAME + "\""+ "." +"\"" + tableName + "\"";
		
		System.out.print("Please specify all columns in order of which you want to insert : ");
		colList = sc.nextLine();
		
		System.out.print("Please specify values for each column : ");
		valueList = sc.nextLine();
		
		/*
		 * jdbc를 통해 insert 작업 실행
		 * <예외처리>
		 * 만일 동일한 튜플이 이미 있을 시  successOrNot = false로 설정하고 insert문 실행 중지
		 * insert문이 실행 완료되었을 시 successOrNot = true로 설정하기
		 */
		// 오류 확인을 위해 Column list만 뽑아내기
		ArrayList<String> colArrList = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(colList, ", ");
		while (st.hasMoreTokens()) {
			colArrList.add(st.nextToken());
		}
		
		// 성공 시 성공 메시지 출력, 실패 시 실패 메시지 출력
		String InsertSQL = "INSERT INTO " + tableName + " (";
		
		// Column 관련된 부분들 "" 씌우기	
		for(int i = 0; i < colArrList.size() - 1; i++) {
			InsertSQL = InsertSQL + "\"" + colArrList.get(i) + "\", ";
		}
		InsertSQL = InsertSQL + "\"" + colArrList.get(colArrList.size() - 1) + "\")";
			
			
		// Value에 관한 부분 '' 씌우기
		InsertSQL = InsertSQL + " VALUES (";
		ArrayList<String> valueArrList = new ArrayList<>();
		st = new StringTokenizer(valueList, ", ");
		while (st.hasMoreTokens()) {
			valueArrList.add(st.nextToken());
		}
			
		for(int i = 0; i < valueArrList.size() - 1; i++) {
			InsertSQL = InsertSQL + "'" + valueArrList.get(i) + "', ";
		}
		InsertSQL = InsertSQL + "'" + valueArrList.get(valueArrList.size() - 1) + "')";
			
		// 테스트용도
		// System.out.println(InsertSQL);
			
		/*
		 * 여기서 jdbc 연결
		 */
		try {
			stmt.executeUpdate(InsertSQL);
		} catch(Exception e) {
			System.out.println("<0 row inserted due to error>");
			System.out.println();
			return;
		}
		System.out.println("<1 row inserted>");
		System.out.println();
	}
	
	public static void deleteOperation(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		String tableName = ""; // 테이블 이름, 이걸 from 다음에 넣기
		String whereColName = ""; // where 절의 column들
		
		// where절의 쓰게 될 시 넣어야 하는 값
		int conditionOperatorNum = 0; // 입력을 받을 때는 숫자로 받음
		String conditionOperator = ""; // 입력받은 번호를 기반으로 지정
		String conditionValue = ""; // 비교값
		int logicalOperatorNum = 0;
		String logicalOperator = ""; // AND, OR
		
		String whereStatement = "";
		String whereSQLStatement = "";
		
		int deletedRow = 0; // 조건을 충족한 row이면 삭제될 것임. 이 삭제된 row들의 총 개수(지울 때마다 increment 시켜라)
		
		//*******************************************************************************//
		// [입력 Part]
		// 1. 테이블명 받기 
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		// 테이블 이름에  "" 씌우기
		tableName = "\"" + SCHEMA_NAME + "\""+ "." +"\"" + tableName + "\"";
		
		// 3. where 절의 column명 받기
		boolean conditionRun = true;
		
		sc = new Scanner(System.in);
		System.out.print("Please specify the column which you want to make condition (Press enter : skip) : ");
		whereColName = sc.nextLine();
		
		// 그냥 엔터 했을 시는 패스, 입력 받았을 시에는 3.finish 입력 전까지는 while문 돌리기
		if (!whereColName.equals("")) {
			// 1. 연산자 입력 받기
			System.out.print("Please specify the condition (1: =, 2: >, 3: < , 4: >=, 5: <=, 6: !=, 7: LIKE) : ");
			conditionOperatorNum = sc.nextInt();
			// 연산자를 번호로 받았기에 String으로 바꾸기
			switch (conditionOperatorNum) {
			case 1:
				conditionOperator = "=";
				break;
			case 2:
				conditionOperator = ">";
				break;
			case 3:
				conditionOperator = "<";
				break;
			case 4:
				conditionOperator = ">=";
				break;
			case 5:
				conditionOperator = "<=";
				break;
			case 6:
				conditionOperator = "!=";
				break;
			case 7:
				conditionOperator = "LIKE";
				break;
			}
			
			// 2. 인터페이스에 표시될 문장 만들기
			// "COURSE_ID = "까지만 들어감
			whereStatement = whereStatement + whereColName + " " + conditionOperator + " " ;
			// 실제 SQL문에는 컬럼명에 ""를 씌우기
			whereSQLStatement = whereSQLStatement + "\"" + whereColName + "\" " + conditionOperator + " " ;
			
			// 3. 비교값 입력, preparedStatement에 들어가기에 String으로 받아도 된다. 예외처리는 필요없다고 가정함
			System.out.print("Please specify the condition value (" + whereStatement + "?) : ");
			sc = new Scanner(System.in); // flush 작업
			conditionValue = sc.nextLine();
			
			// 4. And, Or, finish 결정. 이 역시 Switch문으로 처리
			System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
			logicalOperatorNum = sc.nextInt();
			switch (logicalOperatorNum) {
			case 1:
				logicalOperator = "and";
				conditionRun = true;
				break;
			case 2:
				logicalOperator = "or";
				conditionRun = true;
				break;
			case 3:
				logicalOperator = "";
				conditionRun = false;
				break;
			}
			
			// 5. whereStatement 재수정
			// "COURSE_ID = 100 or " 까지만 생성
			whereStatement = whereStatement + conditionValue + " " + logicalOperator + " ";
			whereSQLStatement = whereSQLStatement + "'" + conditionValue + "' " + logicalOperator + " ";
			
			while(conditionRun) {
				sc = new Scanner(System.in);
				System.out.print("Please specify the column which you want to make condition : ");
				whereColName = sc.nextLine();
				
				// 그냥 엔터 했을 시는 패스, 입력 받았을 시에는 3.finish 입력 전까지는 while문 돌리기
				if (!whereColName.equals("")) {
					// 1. 연산자 입력 받기
					System.out.print("Please specify the condition (1: =, 2: >, 3: < , 4: >=, 5: <=, 6: !=, 7: LIKE) : ");
					conditionOperatorNum = sc.nextInt();
					// 연산자를 번호로 받았기에 String으로 바꾸기
					switch (conditionOperatorNum) {
					case 1:
						conditionOperator = "=";
						break;
					case 2:
						conditionOperator = ">";
						break;
					case 3:
						conditionOperator = "<";
						break;
					case 4:
						conditionOperator = ">=";
						break;
					case 5:
						conditionOperator = "<=";
						break;
					case 6:
						conditionOperator = "!=";
						break;
					case 7:
						conditionOperator = "LIKE";
						break;
					}
					
					// 2. 인터페이스에 표시될 문장 만들기
					// "COURSE_ID = "까지만 들어감
					whereStatement = whereStatement + whereColName + " " + conditionOperator + " " ;
					// 실제 SQL문에는 컬럼명에 ""를 씌우기
					whereSQLStatement = whereSQLStatement + "\"" + whereColName + "\" " + conditionOperator + " " ;
					
					// 3. 비교값 입력, preparedStatement에 들어가기에 String으로 받아도 된다. 예외처리는 필요없다고 가정함
					System.out.print("Please specify the condition value (" + whereStatement + "?) : ");
					sc = new Scanner(System.in); // flush 작업
					conditionValue = sc.nextLine();
					
					// 4. And, Or, finish 결정. 이 역시 Switch문으로 처리
					System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
					logicalOperatorNum = sc.nextInt();
					switch (logicalOperatorNum) {
					case 1:
						logicalOperator = "and";
						conditionRun = true;
						break;
					case 2:
						logicalOperator = "or";
						conditionRun = true;
						break;
					case 3:
						logicalOperator = "";
						conditionRun = false;
						break;
					}
					
					// 5. whereStatement 재수정
					// "COURSE_ID = 100 or " 까지만 생성
					whereStatement = whereStatement + conditionValue + " " + logicalOperator + " ";
					whereSQLStatement = whereSQLStatement + "'" + conditionValue + "' " + logicalOperator + " ";
			
				}
			}	
		}
				
		whereSQLStatement = whereSQLStatement.trim();
				
		// 입력을 다 마친 후에 preparedStatement에 where절 추가
		/*
		 * 나중에 작업해야함
		 */
				
		// sc 변수를 flush하기
		sc = new Scanner(System.in);
				
					
		// 테스트 용도
		//System.out.println(tableName);
		//System.out.println(whereStatement);
				
		//************************************************************************************//
		// [출력 Part]
				
		// 2. 아마 이 즈음에서 delete문 작업을 해야 할듯
		String DeleteSQL = "DELETE FROM " + tableName;
		
		if(!whereSQLStatement.equals("")){
			DeleteSQL = DeleteSQL + " WHERE " + whereSQLStatement;
		}
		
		// System.out.println(DeleteSQL);
		
		/*
		 * deletedRow의 값을 반드시 저장해놔라
		 */
		try {
			deletedRow = stmt.executeUpdate(DeleteSQL);
			// 5. 총 몇 개의 totalRow 읽었는지 출력
			// 0, 1개 일 때는 row, 2개 이상일 때는 rows로 출력
			if (deletedRow == 0 || deletedRow == 1) {
				System.out.println("<"+ deletedRow +" row deleted>");
			} else {
				System.out.println("<"+ deletedRow +" rows deleted>");
			}
			System.out.println();
			
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("<error detected>");
			System.out.println();
			return; // 함수 강제 종료
		}
		
	}
	
	public static void updateOperation(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		String tableName = ""; // 테이블 이름, 이걸 from 다음에 넣기
		String whereColName = ""; // where 절의 column들
		
		// where절의 쓰게 될 시 넣어야 하는 값
		int conditionOperatorNum = 0; // 입력을 받을 때는 숫자로 받음
		String conditionOperator = ""; // 입력받은 번호를 기반으로 지정
		String conditionValue = ""; // 비교값
		int logicalOperatorNum = 0;
		String logicalOperator = ""; // AND, OR
		
		String updateColName = ""; // set절의 Column들
		String updateColValue = ""; // set절에서 넣을 값들
		
		String whereStatement = "";
		String whereSQLStatement = "";
		
		int updatedRow = 0; // 조건을 충족한 row이면 삭제될 것임. 이 삭제된 row들의 총 개수(지울 때마다 increment 시켜라)
		
		//*******************************************************************************//
		// [입력 Part]
		// 1. 테이블명 받기 
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		// 테이블 이름에  "" 씌우기
		tableName = "\"" + SCHEMA_NAME + "\""+ "." + "\"" + tableName + "\"";
		
		// 3. where 절의 column명 받기
		boolean conditionRun = true;
		
		sc = new Scanner(System.in);
		System.out.print("Please specify the column which you want to make condition (Press enter : skip) : ");
		whereColName = sc.nextLine();
		
		// 그냥 엔터 했을 시는 패스, 입력 받았을 시에는 3.finish 입력 전까지는 while문 돌리기
		if (!whereColName.equals("")) {
			// 1. 연산자 입력 받기
			System.out.print("Please specify the condition (1: =, 2: >, 3: < , 4: >=, 5: <=, 6: !=, 7: LIKE) : ");
			conditionOperatorNum = sc.nextInt();
			// 연산자를 번호로 받았기에 String으로 바꾸기
			switch (conditionOperatorNum) {
			case 1:
				conditionOperator = "=";
				break;
			case 2:
				conditionOperator = ">";
				break;
			case 3:
				conditionOperator = "<";
				break;
			case 4:
				conditionOperator = ">=";
				break;
			case 5:
				conditionOperator = "<=";
				break;
			case 6:
				conditionOperator = "!=";
				break;
			case 7:
				conditionOperator = "LIKE";
				break;
			}
			
			// 2. 인터페이스에 표시될 문장 만들기
			// "COURSE_ID = "까지만 들어감
			whereStatement = whereStatement + whereColName + " " + conditionOperator + " " ;
			// 실제 SQL문에는 컬럼명에 ""를 씌우기
			whereSQLStatement = whereSQLStatement + "\"" + whereColName + "\" " + conditionOperator + " " ;
			
			// 3. 비교값 입력, preparedStatement에 들어가기에 String으로 받아도 된다. 예외처리는 필요없다고 가정함
			System.out.print("Please specify the condition value (" + whereStatement + "?) : ");
			sc = new Scanner(System.in); // flush 작업
			conditionValue = sc.nextLine();
			
			// 4. And, Or, finish 결정. 이 역시 Switch문으로 처리
			System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
			logicalOperatorNum = sc.nextInt();
			switch (logicalOperatorNum) {
			case 1:
				logicalOperator = "and";
				conditionRun = true;
				break;
			case 2:
				logicalOperator = "or";
				conditionRun = true;
				break;
			case 3:
				logicalOperator = "";
				conditionRun = false;
				break;
			}
			
			// 5. whereStatement 재수정
			// "COURSE_ID = 100 or " 까지만 생성
			whereStatement = whereStatement + conditionValue + " " + logicalOperator + " ";
			whereSQLStatement = whereSQLStatement + "'" + conditionValue + "' " + logicalOperator + " ";
			
			while(conditionRun) {
				sc = new Scanner(System.in);
				System.out.print("Please specify the column which you want to make condition : ");
				whereColName = sc.nextLine();
				
				// 그냥 엔터 했을 시는 패스, 입력 받았을 시에는 3.finish 입력 전까지는 while문 돌리기
				if (!whereColName.equals("")) {
					// 1. 연산자 입력 받기
					System.out.print("Please specify the condition (1: =, 2: >, 3: < , 4: >=, 5: <=, 6: !=, 7: LIKE) : ");
					conditionOperatorNum = sc.nextInt();
					// 연산자를 번호로 받았기에 String으로 바꾸기
					switch (conditionOperatorNum) {
					case 1:
						conditionOperator = "=";
						break;
					case 2:
						conditionOperator = ">";
						break;
					case 3:
						conditionOperator = "<";
						break;
					case 4:
						conditionOperator = ">=";
						break;
					case 5:
						conditionOperator = "<=";
						break;
					case 6:
						conditionOperator = "!=";
						break;
					case 7:
						conditionOperator = "LIKE";
						break;
					}
					
					// 2. 인터페이스에 표시될 문장 만들기
					// "COURSE_ID = "까지만 들어감
					whereStatement = whereStatement + whereColName + " " + conditionOperator + " " ;
					// 실제 SQL문에는 컬럼명에 ""를 씌우기
					whereSQLStatement = whereSQLStatement + "\"" + whereColName + "\" " + conditionOperator + " " ;
					
					// 3. 비교값 입력, preparedStatement에 들어가기에 String으로 받아도 된다. 예외처리는 필요없다고 가정함
					System.out.print("Please specify the condition value (" + whereStatement + "?) : ");
					sc = new Scanner(System.in); // flush 작업
					conditionValue = sc.nextLine();
					
					// 4. And, Or, finish 결정. 이 역시 Switch문으로 처리
					System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
					logicalOperatorNum = sc.nextInt();
					switch (logicalOperatorNum) {
					case 1:
						logicalOperator = "and";
						conditionRun = true;
						break;
					case 2:
						logicalOperator = "or";
						conditionRun = true;
						break;
					case 3:
						logicalOperator = "";
						conditionRun = false;
						break;
					}
					
					// 5. whereStatement 재수정
					// "COURSE_ID = 100 or " 까지만 생성
					whereStatement = whereStatement + conditionValue + " " + logicalOperator + " ";
					whereSQLStatement = whereSQLStatement + "'" + conditionValue + "' " + logicalOperator + " ";
			
				}
			}	
		}
				
		whereSQLStatement = whereSQLStatement.trim();
		
				
		// sc 변수를 flush하기
		sc = new Scanner(System.in);
		
		// 4. set 절의 column명 받기
		System.out.print("Please specify column names which you want to update : ");
		updateColName = sc.nextLine();
		System.out.print("Please specify the value which you want to put : ");
		updateColValue = sc.nextLine();
		
		
		ArrayList<String> colNameList = new ArrayList<>();
		ArrayList<String> colValueList = new ArrayList<>();
		
		StringTokenizer st = new StringTokenizer(updateColName, " ,");
		while (st.hasMoreTokens()) {
			colNameList.add(st.nextToken());
		}
		
		st = new StringTokenizer(updateColValue, " ,");
		while (st.hasMoreTokens()) {
			colValueList.add(st.nextToken());
		}
				
			
		// 테스트 용도
//		System.out.println(tableName);
//		System.out.println(whereStatement);
//		System.out.println(updateColName);
//		System.out.println(updateColValue);
		
		//************************************************************************************//
		// [출력 Part]
		
		// 2. 아마 이 즈음에서 update문 작업을 해야 할듯
		String UpdateSQL = "UPDATE " + tableName + " SET ";
		for(int i = 0; i < colNameList.size() - 1; i++) {
			UpdateSQL = UpdateSQL + "\"" +  colNameList.get(i) + "\" = '" + colValueList.get(i) + "', ";
		}
		UpdateSQL = UpdateSQL + "\"" + colNameList.get(colNameList.size() - 1) + "\" = '" + colValueList.get(colNameList.size() - 1) + "'";
		
		if(!whereSQLStatement.equals("")){
			UpdateSQL = UpdateSQL +" WHERE " + whereSQLStatement;
		}
		
		// System.out.println(UpdateSQL);
		
		/*
		 * updatedRow의 값을 반드시 저장해놔라
		 */
		try {
			updatedRow = stmt.executeUpdate(UpdateSQL);
			// 5. 총 몇 개의 totalRow 읽었는지 출력
			// 0, 1개 일 때는 row, 2개 이상일 때는 rows로 출력
			if (updatedRow == 0 || updatedRow == 1) {
				System.out.println("<"+ updatedRow +" row updated>");
			} else {
				System.out.println("<"+ updatedRow +" rows updated>");
			}
			System.out.println();
			
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("<error detected>");
			System.out.println();
			return; // 함수 강제 종료
		}
	}
	
	public static void dropTable(Statement stmt) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		String tableName = ""; // 테이블 이름
		String yesOrNo = "";
		
		System.out.print("Please specify the table name : ");
		tableName = sc.nextLine();
		
		sc = new Scanner(System.in);
		
		while(true){
			System.out.print("If you delete this table, it is not guaranteed to recover again. Are you sure you want to delete this table (Y: yes, N: no)? : ");
			yesOrNo = sc.nextLine();
			if (yesOrNo.equals("N")) {
				System.out.println("<Deletion canceled>");
				System.out.println();
				return;
			} else if (yesOrNo.equals("Y")) {
				String DropTableSQL = "DROP TABLE " + "\"" + SCHEMA_NAME + "\"" + "." + "\"" + tableName+ "\"";
				// System.out.println(DropTableSQL);
				/*
				 * String preSmt = "drop table " + tableName;
				 * 그 후 jdbc 실행
				 */
				stmt.executeUpdate(DropTableSQL);
				System.out.println("<The table " + tableName + " is deleted>");
				System.out.println();
				return;
			} 
		}
		
	}
}
