package application;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

public class Contoller {

	private DatabaseAccess db = new DatabaseAccess();
	@FXML private TextField name;
	@FXML private ScrollPane displayNames;
	@FXML private TextField number;
	private boolean setLabelText = true;
	@FXML private Button editText;
	@FXML private String editName;
	@FXML private String editNumber;
	@FXML private TextField addName;
	@FXML private TextField addNumber;
	@FXML private TextField inputElement;
	@FXML private ArrayList<Button> buttonArray = new ArrayList<>();

	/**
	 *
	 * @throws SQLException
	 */
	public void initialize() throws SQLException{
		displayContacts(new ActionEvent());
	}

	@FXML
	/**
	 *
	 * @param e
	 * @throws SQLException
	 */
	public void addNewContact(ActionEvent e) throws SQLException{
		name.setText("");
		name.setPromptText("Enter name here...");

		number.setText("");
		number.setPromptText("Enter Number...");

	}


	@FXML
	/**
	 *
	 * @param e
	 * @throws SQLException
	 */
	public void searchContact(ActionEvent e) throws SQLException{
		String search = inputElement.getText();

		PreparedStatement ps = db.getConnection().prepareStatement("SELECT * FROM PhoneBookRecords WHERE name = ? OR phoneNumber = ?");
		ps.setString(1, search);
		ps.setString(2, search);

		ResultSet rs = ps.executeQuery();

		if(rs.isAfterLast() == rs.isBeforeFirst()){
			Alert alert = new Alert(AlertType.INFORMATION, "No match Found");
			alert.showAndWait();
		}
		else{
			Alert alert = new Alert(AlertType.INFORMATION, "Match Found");
			alert.showAndWait();
			for(Button i : buttonArray){
				if(i.getText().equalsIgnoreCase(search)){
					i.fire();
					break;
				}
			}
		}
		inputElement.setText("");
		inputElement.setFocusTraversable(true);
		inputElement.setPromptText("Search Contact...");
	}

	@FXML
	/**
	 *
	 * @param e
	 * @throws SQLException
	 * @throws IOException
	 */
	public void deleteContact(ActionEvent e) throws SQLException, IOException{
		Alert alert = new Alert(AlertType.CONFIRMATION, "Delete this Contact?", ButtonType.YES, ButtonType.NO);
		alert.showAndWait();
		if(alert.getResult() == ButtonType.YES){

			PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM PhoneBookRecords WHERE name = ?");
			ps.setString(1, name.getText());
			ps.executeUpdate();

			setLabelText = true;
			displayContacts(e);
		}
	}

	@FXML
	/**
	 *
	 * @param e
	 * @throws SQLException
	 */
	public void displayContacts(ActionEvent e) throws SQLException{
		VBox pane = new VBox();

		ResultSet rs = db.getStatement().executeQuery("SELECT * FROM PhoneBookRecords");
		while(rs.next()){
			Person p = new Person(rs.getString(2), rs.getString(3));

			if(setLabelText){
				name.setText(p.getName());
				number.setText(p.getPhoneNumber());
				setLabelText = false;
				editName = name.getText();
				editNumber = number.getText();
			}

			Button contactName = new Button(p.getName());
			contactName.setMinHeight(50);
			contactName.setMinWidth(185);
			buttonArray.add(contactName);
			contactName.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent e){
					editName = p.getName();
					editNumber = p.getPhoneNumber();
					name.setText(p.getName());
					number.setText(p.getPhoneNumber());
				}
			});
			pane.getChildren().add(contactName);
		}

		displayNames.setContent(pane);
		displayNames.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		displayNames.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);


	}

	@FXML
	/**
	 *
	 * @param e
	 * @throws SQLException
	 */
	public void editContact(ActionEvent e) throws SQLException{

		if(name.getText().equals("") || number.getText().equals("")){
			Alert alert = new Alert(AlertType.INFORMATION, "Fields are Empty. Contact will not be saved.");
			alert.showAndWait();
			buttonArray.get(0).fire();
		}
		else{
			String check = number.getText();

			PreparedStatement prepStatement = db.getConnection().prepareStatement("SELECT * FROM PhoneBookRecords"
					 + " WHERE name= ? OR phoneNumber= ?");
			prepStatement.setString(1, name.getText());
			prepStatement.setString(2, number.getText());

			ResultSet existingContact = prepStatement.executeQuery();

			boolean correctNumber = numberValidator(check);

			if(correctNumber){
				if(existingContact.isAfterLast() == existingContact.isBeforeFirst()){

					PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO PhoneBookRecords (name, phoneNumber) VALUES (?, ?)");
					ps.setString(1, name.getText());
					ps.setString(2, number.getText());
					ps.executeUpdate();

					Alert alert = new Alert(AlertType.INFORMATION, "Contact Added.");
					alert.showAndWait();
				}
				else{

					PreparedStatement ps = db.getConnection().prepareStatement("UPDATE PhoneBookRecords SET name = ?, phoneNumber = ? WHERE name= ? AND phoneNumber = ?");
					ps.setString(1, name.getText());
					ps.setString(2, number.getText());
					ps.setString(3, editName);
					ps.setString(4, editNumber);
					ps.executeUpdate();

					Alert alert = new Alert(AlertType.INFORMATION, "Contact Updated.");
					alert.showAndWait();
				}
			}else{
				Alert alert = new Alert(AlertType.INFORMATION, "Phone Number is Invalid.");
				alert.showAndWait();
				name.setText(editName);
				number.setText(editNumber);
			}


			displayContacts(e);
		}
	}

	/**
	 *
	 * @param check
	 * @return
	 */
	private boolean numberValidator(String check){

		for(int i = 0; i < check.length(); i++){
			if(!(Character.isDigit(check.charAt(i)) || check.charAt(i) == '-' || check.charAt(i) == '+')){
				return false;
			}
		}
		return true;
	}

}
