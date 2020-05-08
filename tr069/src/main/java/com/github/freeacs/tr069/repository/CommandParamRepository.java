package com.github.freeacs.tr069.repository;

import com.github.freeacs.tr069.entity.CommandParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandParamRepository extends JpaRepository<CommandParam, Integer> {

    List<CommandParam> findByCommandIdAndDiagnostic(Integer commandId, boolean diagnostic);
}
