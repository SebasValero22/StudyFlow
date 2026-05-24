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
    Call<List<TaskResponseDTO>> getTasks(@Query("userId") Integer userId);

    @POST("tasks")
    Call<TaskResponseDTO> createTask(@Body TaskResponseDTO task);

    @PUT("tasks/{id}")
    Call<TaskResponseDTO> updateTask(@Path("id") Integer id, @Body TaskResponseDTO task);

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") Integer id);

    // Subjects
    @GET("subjects")
    Call<List<SubjectResponseDTO>> getSubjects(@Query("userId") Integer userId);

    @POST("subjects")
    Call<SubjectResponseDTO> createSubject(@Body SubjectResponseDTO subject);

    @PUT("subjects/{id}")
    Call<SubjectResponseDTO> updateSubject(@Path("id") Integer id, @Body SubjectResponseDTO subject);

    @DELETE("subjects/{id}")
    Call<Void> deleteSubject(@Path("id") Integer id);

    // Exams
    @GET("exams")
    Call<List<ExamResponseDTO>> getExams(@Query("userId") Integer userId);

    // Grades
    @GET("grades")
    Call<List<GradeResponseDTO>> getGrades(@Query("userId") Integer userId);
}
