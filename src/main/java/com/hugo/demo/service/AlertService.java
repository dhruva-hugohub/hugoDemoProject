package com.hugo.demo.service;

import com.hugo.demo.api.alert.AlertResponseDTO;
import com.hugo.demo.api.alert.CreateAlertRequestDTO;
import com.hugo.demo.api.alert.EditAlertRequestDTO;

public interface AlertService {

    AlertResponseDTO createAlert(CreateAlertRequestDTO dto);

    AlertResponseDTO editAlert(EditAlertRequestDTO dto);
}
