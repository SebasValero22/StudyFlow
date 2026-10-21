package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Integer> {
    List<UserSettings> findByUserId(Integer userId);
    Optional<UserSettings> findByUserIdAndSettingKey(Integer userId, String settingKey);
}
