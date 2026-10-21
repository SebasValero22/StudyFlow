package com.example.studyflow.data.remote;

import com.example.studyflow.utils.UserSession;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

public class ApiClient {

    private static final String BASE_URL = "https://studyflow-nuux.onrender.com/api/";
    private static Retrofit retrofit = null;

    /** Attaches "Authorization: Bearer <jwt>" to every request when logged in. */
    private static final Interceptor authInterceptor = chain -> {
        Request.Builder builder = chain.request().newBuilder();
        String token = UserSession.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return chain.proceed(builder.build());
    };

    public static Retrofit getClient(){
        if (retrofit == null){
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .build();
        }
        return retrofit;
    }
}
