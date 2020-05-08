package com.github.freeacs.tr069.repository;

import com.github.freeacs.tr069.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Device findFirstBySerialNumber(String serialNumber);
}
