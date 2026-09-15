package com.example.studyflow.data.remote;

import com.example.studyflow.data.dto.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Users
    @POST("users/login")
    Call<UserResponseDTO> login(@Body UserLoginDTO loginRequest);

    @POST("users/register")
    Call<UserResponseDTO> register(@Body UserRegisterDTO registerRequest);

    // Tasks
    @GET("tasks")
    Call<List<TaskResponseDTO>> getTasks();

    @POST("tasks")
    Call<TaskResponseDTO> createTask(@Body TaskResponseDTO task);

    @PUT("tasks/{id}")
    Call<TaskResponseDTO> updateTask(@Path("id") Integer id, @Body TaskResponseDTO task);

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") Integer id);

    // Subjects
    @GET("subjects")
    Call<List<SubjectResponseDTO>> getSubjects();

    @POST("subjects")
    Call<SubjectResponseDTO> createSubject(@Body SubjectResponseDTO subject);

    // Exams (Placeholder DTOs if needed or generic)
    @GET("exams")
    Call<List<TaskResponseDTO>> getExams(); // Often similar to tasks in logic

    // Grades
    @GET("grades")
    Call<List<GradeResponseDTO>> getGrades();
}
