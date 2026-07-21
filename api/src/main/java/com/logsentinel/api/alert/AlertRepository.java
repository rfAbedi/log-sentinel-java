package com.logsentinel.api.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    List<AlertEntity> findAllByOrderByTriggeredAtDescIdDesc();
}
