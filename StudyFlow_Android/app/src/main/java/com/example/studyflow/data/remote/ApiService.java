package com.example.studyflow.data.remote;

import com.example.studyflow.data.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // Autenticación
    @POST("users/login")
    Call<User> login(@Body User loginRequest);

    // Tareas
    @GET("tasks")
    Call<List<Task>> getTasks();

    @POST("tasks")
    Call<Task> createTask(@Body Task task);

    // Asignaturas
    @GET("subjects")
    Call<List<Subject>> getSubjects();

    // Exámenes
    @GET("exams")
    Call<List<Exam>> getExams();

    // Calificaciones
    @GET("grades")
    Call<List<Grade>> getGrades();
}