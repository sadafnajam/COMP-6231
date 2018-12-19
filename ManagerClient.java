package client;

import helper.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import ServerOperationApp.ServerOperationIDL;

public class ManagerClient {
	private final int REQUIRED_NUMERIC_ID_LENGTH = 4;
	private static ServerOperationIDL serverImpl;

	public ManagerClient() {
		new File("./Managerlogs").mkdirs();
	}

	public static NamingContextExt setUp(String[] args) {
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);
			// get the root naming context
			org.omg.CORBA.Object objRef;
			objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			return ncRef;
		} catch (InvalidName e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		ManagerClient client = new ManagerClient();

		Log log = null;
		NamingContextExt ncRef = setUp(args);
		if (ncRef == null) {
			throw new RuntimeException("Error in binding");
		}
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Please enter your Manager ID or type ex to exit");
			try {
				String managerID = br.readLine();
				if (managerID.toLowerCase().equals("ex")) {
					System.out.println("Good bye");
					break;
				}
				if (client.isManagerIDValid(managerID)) {
					try {
						// System.out.println(managerID);
						log = new Log("./Managerlogs/" + managerID + ".txt", ManagerClient.class.getName() + managerID);
						log.logger.setLevel(Level.ALL);

					} catch (Exception e) {
						System.out.println("Error creating Manager Logger");
					}
					// serverImpl = client.getServerInstance(managerID,ncRef);
					serverImpl = ServerInstance.getServerInstance(managerID, ncRef);

					int option = 0;
					while (option != 6) {
						System.out.println("Please select one of the following ");
						System.out.println("1. Create teacher record");
						System.out.println("2. Create student record");
						System.out.println("3. Get record counts");
						System.out.println("4. Edit a record");
						System.out.println("5. Tansfer a record");
						System.out.println("6. Exit");
						String optionInStringFormat = br.readLine();
						if (!client.isValidOption(optionInStringFormat)) {
							System.out.println("Please select a correct input");
							continue;
						}
						option = Integer.parseInt(optionInStringFormat);
						if (option == 6) {
							log = null;
							continue;
						} else if (option == 1) {
							// Note all validation logic would be at the server
							// ex phone not being 10 digit, location not being
							// correct
							System.out.println(
									"Please enter  first name. Should not be empty and no special symbol allowed");
							String firstName = br.readLine();
							System.out.println(
									"Please enter last name. Should not be empty and no special symbol allowed");
							String lastName = br.readLine();
							System.out.println("Please enter adress");
							String address = br.readLine();
							System.out.println("Please enter phone number. Must be 10 digit");
							String phone = br.readLine();
							System.out.println("Please enter specialization");
							String specialization = br.readLine();
							System.out.println("Please enter location (mtl, lvl, ddo)");
							String location = br.readLine();
							String message = serverImpl.createTRecord(managerID, firstName, lastName, address, phone,
									specialization, location);
							System.out.println(message + "\n");

							// LOG message here
							log.logger.log(Level.INFO, message);
						} else if (option == 2) {
							System.out.println(
									"Please enter first name. Should not be empty and no special symbol allowed");
							String firstName = br.readLine();
							System.out.println(
									"Please enter last name. Should not be empty and no special symbol allowed");
							String lastName = br.readLine();
							System.out.println("Please enter comma seperated courses the student has registered");
							String commaSeperateList = br.readLine();
							List<String> coursesRegistered = client
									.convertCommaSeperatedStringToList(commaSeperateList);
							System.out.println("Please enter status");
							System.out.println("1 Active");
							System.out.println("2 Inactive");
							String statusString = br.readLine();
							DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
							LocalDateTime now = LocalDateTime.now();
							// System.out.println(dtf.format(now));
							String statusDate = dtf.format(now);
							String[] tempArray = new String[coursesRegistered.size()];
							String message = serverImpl.createSRecord(managerID, firstName, lastName,
									coursesRegistered.toArray(tempArray), statusString, statusDate);
							System.out.println(message + "\n");
							// Logger
							log.logger.log(Level.INFO, message);
						} else if (option == 3) {
							String message = serverImpl.getRecordCounts(managerID);
							System.out.println(message + "\n");
							// Logger
							log.logger.log(Level.INFO, message);
						} else if (option == 4) {
							System.out.println("Information");
							System.out.println(
									"The fields of teacher record that could be edited are-> address, phone (10 digit number) and location[mtl, lvl or ddo]");
							System.out.println(
									"The fields of student record that could be edited are-> course and status[1=Active, 2=InActive]");
							System.out.println("Please enter recordID");
							String recordID = br.readLine();
							System.out.println("Please enter field name (Incorrect field name would result in error)");
							String fieldName = br.readLine();
							System.out.println(
									"Please enter new value for the field (Incorrect data would result in error)");
							String newValue = br.readLine();
							String message = serverImpl.editRecord(managerID, recordID, fieldName, newValue);
							System.out.println(message + "\n");
							log.logger.log(Level.INFO, message);
						} else if (option == 5) {
							System.out.println("Please enter recordID");
							String recordID = br.readLine();
							System.out.println("Please enter server where record is to be transferred(mtl, lvl, ddo)");
							String serverName = br.readLine();
							String message = serverImpl.transferRecord(managerID, recordID, serverName);
							System.out.println(message + "\n");
							log.logger.log(Level.INFO, message);
						}
					}
				} else {
					System.out.println("Please enter a valid Manager ID");
					System.out
							.println("Manager ID should be location name (MTL, LVL, DDO) followed by a 4 digit number");
					try {
						log = new Log("./Managerlogs/failedLogin.txt", ManagerClient.class.getName());
						log.logger.setLevel(Level.ALL);
						log.logger.log(Level.INFO, "Failed login attempt by " + managerID
								+ " as it does not match the format of a valid Manager ID");
					} catch (Exception e) {
						System.out.println("Error creating Manager Logger");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean isManagerIDValid(String managerID) {
		if (managerID.length() < 7) {
			return false;
		}
		String locationPart = managerID.substring(0, 3).toLowerCase();
		try {
			ServerNameEnum.valueOf(locationPart);
		} catch (IllegalArgumentException ex) {
			return false;
		}

		String numericPart = managerID.substring(3);
		int lengthOfNumericPart = numericPart.length();
		if (lengthOfNumericPart != REQUIRED_NUMERIC_ID_LENGTH) {
			return false;
		} else {
			try {
				Integer.parseInt(numericPart);
				return true;
			} catch (NumberFormatException ex) {
				return false;
			}
		}
	}

	public boolean isValidOption(String option) {
		try {
			int optionNo = Integer.parseInt(option);
			if (1 <= optionNo && optionNo <= 6) {
				return true;
			}
			return false;

		} catch (NumberFormatException ex) {

			return false;
		}
	}

	public List<String> convertCommaSeperatedStringToList(String commaSeperatedCourses) {
		String[] arr = commaSeperatedCourses.split(",");
		List<String> arrayList = new ArrayList<String>();
		for (String s : arr) {
			arrayList.add(s);
		}
		return arrayList;
	}

	public boolean isValid(Predicate<String> p, String s) {
		return p.test(s);
	}

}
