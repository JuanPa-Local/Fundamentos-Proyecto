package com.openlib.backend.domain.order;

import com.openlib.backend.application.service.DescargaFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/downloads")
public class DescargaRestController {

    private final DescargaFacade descargaFacade;

    public DescargaRestController(DescargaFacade descargaFacade) {
        this.descargaFacade = descargaFacade;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateToken(@RequestBody Map<String, String> body) {
        UUID buyerId = UUID.fromString(body.get("buyerId"));
        UUID libroId = UUID.fromString(body.get("libroId"));
        
        EnlaceDescarga enlace = descargaFacade.generarEnlace(buyerId, libroId);
        return ResponseEntity.ok(Map.of("token", enlace.getToken()));
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, String>> executeDownload(@PathVariable String token, HttpServletRequest request) {
        String ipOrigen = request.getRemoteAddr();
        String downloadUrl = descargaFacade.ejecutarDescarga(token, ipOrigen);
        return ResponseEntity.ok(Map.of("url", downloadUrl));
    }
}
