package com.myprojects.callrecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class CallRecord extends Application {
        
    @Override
    public void start(final Stage stage) {
        
        //Defining Constructor Arguments 
        String welcomeTitle = "Welcome";
        String welcomeMessage = "This is a tool for submitting Zendesk tickets. "
                              + "When you submit a ticket, it will appear under the 'Tickets Requiring "
                              + "Your Attention' view in Zendesk, and there you will be identified as the ticket requester. "
                              + "Press any key to continue.";
        
        //confimation window text
        String confirmTitle = "Ticket Submitted!";
        String confirmMessage = "Check your Zendesk "
                              + "account to view the created ticket. You may now press any key to "
                              + "return to the previous screen.";
       
        //Initializing Application Scenes    
        
        //1. Initialize application start page
        final Notification welcomePage = new Notification(welcomeTitle, welcomeMessage);

        //2. Initiailize input page for name, company, and issue fields
        final TicketForm ticketForm = new TicketForm();
        
        //3. Initialize creds page, along with credentials import
        final CredsForm credsForm = new CredsForm();
        
        //4. Initialize confirmation page
        final Notification confirmPage = new Notification(confirmTitle, confirmMessage);
       
        //Event Handlers for User Interaction 
        
        //1.'Enter' Key Handlers
        
        //inputScene 'Enter' key event handler
        ticketForm.inputScene.setOnKeyPressed(new EventHandler <KeyEvent>() {
            @Override
            public void handle (KeyEvent keyEvent) {
                if (KeyCode.ENTER == keyEvent.getCode()){
                        
                        //make request  
                        try {
                            ticketForm.makeInputRequest(credsForm.sessionCreds);
                         } catch (IOException ex){
                             System.out.println(ex);
                             credsForm.inputTitle.setText("Issue Encountered – Sorry!");
                             credsForm.inputSubtitle.setText("Your Zendesk Credentials May Be Invalid");
                             stage.setScene(credsForm.inputScene);
                         }
                       
                        //status-line=bad, send user to credentials page - otherwise use confirmation page
                        if (!ticketForm.responseLine.equals("HTTP/1.1 201 Created")) {
                            credsForm.inputTitle.setText("Issue Encountered – Sorry!");
                            credsForm.inputSubtitle.setText("Your Zendesk Credentials May Be Invalid");
                            stage.setScene(credsForm.inputScene);
                        }
                        else {
                            stage.setScene(confirmPage.scene);
                       }
                    }
                }
        });
        
        //confirmScene 'Enter' key event handler
        confirmPage.scene.setOnKeyPressed(new EventHandler <KeyEvent>() {
            @Override
            public void handle (KeyEvent keyEvent) {
                //reset question fields to empty
                ticketForm.input_1.setText("");
                ticketForm.input_1.requestFocus();
                ticketForm.input_2.setText("");
                ticketForm.input_3.setText("");

                stage.setScene(ticketForm.inputScene);          
            }
        });
        
        //inputScene 'Enter' key event handler
        credsForm.inputScene.setOnKeyPressed(new EventHandler <KeyEvent>() {
            @Override
            public void handle (KeyEvent keyEvent) {
                if (KeyCode.ENTER == keyEvent.getCode()){
                             //write creds to file
                             credsForm.writeCreds();
                             //read creds again
                             credsForm.sessionCreds = credsForm.readCreds();
                             stage.setScene(ticketForm.inputScene);
                    }
                }
        });
        
        //2. Submit Button Handlers
        
        //inputScene Submit button event handler
        ticketForm.submit.setOnAction(new EventHandler<ActionEvent> () {
          
            @Override
            public void handle(ActionEvent event) {
                        //make request
                        try {
                            ticketForm.makeInputRequest(credsForm.sessionCreds);
                         } catch (IOException ex){
                             System.out.println(ex);
                             credsForm.inputTitle.setText("Issue Encountered – Sorry!");
                             credsForm.inputSubtitle.setText("Your Zendesk Credentials May Be Invalid");
                             stage.setScene(credsForm.inputScene);
                         }
                       
                        //status-line=bad, send user to credentials page - otherwise use confirmation page
                        if (!ticketForm.responseLine.equals("HTTP/1.1 201 Created")) {
                            credsForm.inputTitle.setText("Issue Encountered - Sorry!");
                            credsForm.inputSubtitle.setText("Your Zendesk Credentials May Be Invalid");
                            stage.setScene(credsForm.inputScene);
                        }
                        else {
                            stage.setScene(confirmPage.scene);
                        }
            }});
        
        //inputScene Submit button event handler
        credsForm.submit.setOnAction(new EventHandler<ActionEvent> () {

            @Override
            public void handle(ActionEvent event) {
                //write creds to file
                credsForm.writeCreds();
                //read creds again
                credsForm.sessionCreds = credsForm.readCreds();
                System.out.println(Arrays.toString(credsForm.sessionCreds));
                stage.setScene(ticketForm.inputScene);
            }
        });
        
        //3.General Key Event Handler for Start Page
        
        welcomePage.scene.setOnKeyPressed(new EventHandler<KeyEvent> () {
            @Override
            public void handle(KeyEvent keyEvent) {
                //if ZenDesk credentials haven't been recorded, take user to credentials screen
                if (Arrays.toString(credsForm.sessionCreds).equals("[null, null, null]")) {
                    stage.setScene(credsForm.inputScene);
                //otherwise take the user straight to the create ticket screen
                } else {
                    stage.setScene(ticketForm.inputScene);
                }
            }
    });

        
        //Setting Initial Scene
        stage.setScene(welcomePage.scene);
        stage.show();       
    }
    
    public static void main(final String[] args) {
        launch(args);
    }
}

class InputForm {

    //GridPane parent node and containing scene
    
    //initializing scene
    GridPane inputGrid = new GridPane();
    Scene inputScene = new Scene(inputGrid, 400, 300);

    //text strings for field labels
    String input_1_labelString = "Name";
    String input_2_labelString = "Company";
    String input_3_labelString = "Subject";
    String inputTitleString = "Create New Ticket";
    String inputSubtitleString;
    String buttonText;
    
    //text field nodes
    TextField input_1 = new TextField();
    TextField input_2 = new TextField();
    TextField input_3 = new TextField();

    //button node and wrapper
    Button submit = new Button(buttonText);
    HBox hbSubmit = new HBox(10);

    //text nodes
    Text input_1_label = new Text(input_1_labelString);
    Text input_2_label = new Text(input_2_labelString);
    Text input_3_label = new Text(input_3_labelString);
    Text inputTitle = new Text(inputTitleString);
    Text inputSubtitle = new Text(inputSubtitleString);
    
    //HTTP Request Data
    String responseLine;

    public InputForm () {

        //adding nodes to inputGrid
        inputGrid.add(inputTitle, 0, 0, 2, 1);
        inputGrid.add(inputSubtitle, 0, 1, 2, 1);
        inputGrid.add(input_1_label, 0, 2);
        inputGrid.add(input_2_label, 0, 3);
        inputGrid.add(input_3_label, 0, 4);
        inputGrid.add(input_1, 1, 2);
        inputGrid.add(input_2, 1, 3);
        inputGrid.add(input_3, 1, 4);
        
        hbSubmit.setAlignment(Pos.BOTTOM_RIGHT);
        hbSubmit.getChildren().add(submit);
        inputGrid.add(hbSubmit, 1, 5);

        //setting additional positional properties
        inputGrid.setAlignment(Pos.CENTER);
        inputGrid.setHgap(13);
        inputGrid.setVgap(13);
        inputGrid.setPadding(new Insets(25, 25, 25, 25));
        
        //styling
        inputTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 23));
        inputTitle.setFill(Color.ORANGE);
        
        input_1_label.setFill(Color.BLUE);
        input_1_label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        input_2_label.setFill(Color.BLUE);
        input_2_label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        input_3_label.setFill(Color.BLUE);
        input_3_label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        }
}

class TicketForm extends InputForm {
    
    //HTTP Request Data
    String responseLine;
    
    public TicketForm() {
        super();
        submit.setText("Submit");
    }

    public void makeInputRequest (String[] creds) throws IOException {
        
        //Setting Request Data
        
        //http request header fields
        String endpoint = "https://" + creds[0] +  "/api/v2/tickets.json";
        String contentTypeField = "application/json";
        String acceptField = "application/json";
        
        //variables for log date and time
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        DateFormat timeFormat = new SimpleDateFormat("h:mm a");
        String dateString = dateFormat.format(cal.getTime());
        String timeString = timeFormat.format(cal.getTime());
        
        //json array (request body)      
        String subjectKeyValue = this.input_3.getText();
        String bodyKeyValue = "Ticket logged for "+ this.input_1.getText() 
                            + " with " + this.input_2.getText() + " at " + timeString + " on " + dateString;
        
        String jsonArray = "{\"ticket\": "
                            + "{\"subject\": \"" + subjectKeyValue + "\", " 
                            + "\"comment\": { \"body\": \"" + bodyKeyValue + "\","  
                            +                "\"public\":\"false\"}"
                            +"}";
        
        //Builing HTTP Client 
        
        //setting credentials provided in creds parameter
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(creds[0], -1),
            new UsernamePasswordCredentials(creds[1], creds[2]));
        
        //creating HTTP client object
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        try {
            //initiating POST request object with endpoint (defined above)
            HttpPost httpPost = new HttpPost(endpoint);

            //adding JSON object to POST request message 
            httpPost.setEntity(new StringEntity(jsonArray));

            //adding header fields to POST request httpPost
            httpPost.addHeader("Content-Type", contentTypeField);
            httpPost.addHeader("Accept", acceptField);

            //executing request and response processing
            System.out.println("Executing request " + httpPost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                //outputting response data
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println(EntityUtils.toString(response.getEntity()));
                responseLine = response.getStatusLine().toString();
         
            } finally {
                response.close(); 
            }
        } finally {
            httpclient.close();
        }

    }
}

class CredsForm extends InputForm {
    
    String[] sessionCreds;
    
    public CredsForm() {
        super();
        sessionCreds = this.readCreds();
        inputSubtitle.setText("Please Provide your Zendesk Credentials");
        submit.setText("Save");
    }
    
    public void writeCreds () {
        //Writes and Stores the Provided Credentials in ZenDeskCreds_for_App.txt File
        try {
         
            //create password file if this doesn't exist already
            File file = new File("ZenDeskCreds_for_App.txt"); 
            if (!file.exists()) {
                file.createNewFile();
            }
        
            //preparing the file and the file writer objects
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            
            //writing to the file
            bw.write(this.input_1.getText() + "\n" + this.input_2.getText() + "\n" + this.input_3.getText());
            
            //releasing the connection
            bw.close();
            
            
        } catch (IOException e) {}
    }
    
    public String[] readCreds() {
        
        //Reads the Creds from ZenDeskCreds_for_App.txt File and Stores in Array
        
        //credentials array
        String[] creds = new String[3];
        
        try {
            
                //create password file if this doesn't exist already
                File file = new File("ZenDeskCreds_for_App.txt"); 
                if (!file.exists()) {
                    file.createNewFile();
                }            
                              
                //preparing the file reader objects
                FileReader fr = new FileReader("ZenDeskCreds_for_App.txt");
                BufferedReader br = new BufferedReader(fr);
                
                //reading the file
                creds[0] = br.readLine();
                creds[1] = br.readLine();
                creds[2] = br.readLine();
                
                //releasing the connection
                br.close();
                    
            } catch (IOException e) {}
        
        return creds;
    } 
}
    
class Notification {
        
        //Start Page
        
        //scene nodes initialized
        GridPane grid = new GridPane();
        Scene scene  = new Scene(grid, 400, 250);
        
        //text field
        Text messageNode;
        Text titleNode;

        //text strings

        String notificationMessage;
        String notificationTitle;
        
        public Notification(String titleString, String messageString) {
            
            //setting message contents
            notificationMessage = messageString;
            notificationTitle = titleString;
            
            messageNode = new Text(messageString);
            titleNode = new Text(titleString);

            //adding, positioning nodes on grid
            grid.addRow(0,titleNode);
            grid.addRow(1, messageNode);
            messageNode.setWrappingWidth(300);
            
            //font styling
            titleNode.setFont(Font.font("Tahoma", FontWeight.NORMAL, 23));
            titleNode.setFill(Color.ORANGE);
            
            //grid element padding
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(13);
            grid.setVgap(13);
            grid.setPadding(new Insets(25, 25, 25, 25));

        }
}