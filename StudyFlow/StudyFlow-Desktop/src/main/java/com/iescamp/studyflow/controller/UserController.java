package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import javax.swing.table.TableColumn;
import java.util.List;

public class UserController {
    @FXML private TableView<User> userTable ;
    private UserService userService = new UserService();

    @FXML
    public void initialize(){
        loadUsers();
    }

    private void loadUsers(){
        try {

            List<User> users = userService.getAllUsers();
            userTable.getItems().addAll(users);
        }catch (Exception e){
            e.printStackTrace();
            Error error = new Error("no users found");

        }
    }

}
