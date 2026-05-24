package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.ReminderDTO;
import com.iescamp.studyflow_api.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    // Inyeccion del servicio (obligatoria)
    @Autowired
    private ReminderService reminderService;

    // POST /api/reminders
    @PostMapping
    public ReminderDTO add(@RequestBody ReminderDTO dto){
        return reminderService.add(dto);
    }

    // DELETE /api/reminders/5
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        reminderService.delete(id);
    }

    // PUT /api/reminders/5
    @PutMapping("/{id}")
    public ReminderDTO update(@PathVariable Integer id, @RequestBody ReminderDTO dto){
        return reminderService.update(id, dto);
    }

    // GET /api/reminders (devuelve una lista)
    @GetMapping
    public List<ReminderDTO> findAll(){
        return reminderService.findAll(); // Asegurate de que el servicio devuelve List
    }
}
