package com.gestion.reservas.service;

import com.gestion.reservas.dto.*;
import com.gestion.reservas.entity.Espacio;
import com.gestion.reservas.entity.Equipamiento;
import com.gestion.reservas.entity.EstadoEspacio;
import com.gestion.reservas.entity.TipoEspacio;
import com.gestion.reservas.repository.EquipamientoRepository;
import com.gestion.reservas.repository.EspacioRepository;
import com.gestion.reservas.repository.EstadoEspacioRepository;
import com.gestion.reservas.repository.TipoEspacioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;



@Service
@RequiredArgsConstructor
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepo;
    private final TipoEspacioRepository tipoRepo;
    private final EstadoEspacioRepository estadoRepo;
    private final EquipamientoRepository equipamientoRepo;

    @Value("${upload.dir}")
    private String uploadDir;


    @Override
    public List<EspacioResponseDTO> findAll() {
        return espacioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public EspacioResponseDTO findById(Long id) {
        Espacio espacio = espacioRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado"));
        return toDTO(espacio);
    }


    @Override
    /*public EspacioResponseDTO create(EspacioRequestDTO dto) {
        Espacio espacio = toEntity(dto);
        return toDTO(espacioRepo.save(espacio));
    }*/


    public EspacioResponseDTO create(String nombre, int capacidad, String ubicacion,
                                   String descripcion, Long idTipoEspacio, Long idEstado,
                                   List<Long> equipamientos, MultipartFile imagen) {
        Espacio espacio = new Espacio();
        espacio.setNombre(nombre);
        espacio.setCapacidad(capacidad);
        espacio.setUbicacion(ubicacion);
        espacio.setDescripcion(descripcion);

        TipoEspacio tipo = tipoRepo.findById(idTipoEspacio).orElseThrow();
        EstadoEspacio estado = estadoRepo.findById(idEstado).orElseThrow();

        espacio.setTipoEspacio(tipo);
        espacio.setEstado(estado);

        if (equipamientos != null && !equipamientos.isEmpty()) {
            List<Equipamiento> lista = equipamientoRepo.findAllById(equipamientos);
            espacio.setEquipamientos(lista);
        }

        if (imagen != null && !imagen.isEmpty()) {
            // puedes guardar el archivo en disco o en base64 o como prefieras
            String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
            Path ruta = Paths.get("uploads", nombreArchivo);
            try {
                Files.createDirectories(ruta.getParent());
                imagen.transferTo(ruta);
                espacio.setImagen(nombreArchivo); // guarda solo el nombre
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen", e);
            }
        }

        Espacio guardado = espacioRepo.save(espacio);
        return toDTO(guardado);
    }


    public EspacioResponseDTO update(
            Long id,
            String nombre,
            int capacidad,
            String ubicacion,
            String descripcion,
            Long idTipoEspacio,
            Long idEstado,
            List<Long> equipamientos,
            MultipartFile imagen
    ) {
        // 1. Buscar espacio existente
        Espacio espacio = espacioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado con ID: " + id));

        // 2. Actualizar campos
        espacio.setNombre(nombre);
        espacio.setCapacidad(capacidad);
        espacio.setUbicacion(ubicacion);
        espacio.setDescripcion(descripcion);

        // 3. Asociaciones
        espacio.setTipoEspacio(tipoRepo.findById(idTipoEspacio)
                .orElseThrow(() -> new RuntimeException("Tipo de espacio no encontrado")));

        espacio.setEstado(estadoRepo.findById(idEstado)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado")));

        List<Equipamiento> equipamientoList = equipamientoRepo.findAllById(equipamientos);
        espacio.setEquipamientos(equipamientoList);

        // 4. Guardar imagen si hay
        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
            Path ruta = Paths.get("uploads", nombreArchivo);
            try {
                Files.createDirectories(ruta.getParent()); // Asegura carpeta
                imagen.transferTo(ruta); // Guarda archivo
                espacio.setImagen(nombreArchivo); // Solo el nombre se guarda en la entidad
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen", e);
            }
        }

        // 5. Guardar en base de datos
        espacioRepo.save(espacio);

        // 6. Convertir a DTO y retornar
        return toDTO(espacio);
    }


    @Override
    public void delete(Long id) {
        Optional<Espacio> optionalEspacio = espacioRepo.findById(id);
        if (optionalEspacio.isPresent()) {
            Espacio espacio = optionalEspacio.get();

            // 1. Eliminar la imagen si existe
            String nombreImagen = espacio.getImagen();
            if (nombreImagen != null && !nombreImagen.isBlank()) {
                Path rutaImagen = Paths.get(uploadDir).resolve(nombreImagen);
                try {
                    Files.deleteIfExists(rutaImagen);
                    System.out.println("Imagen eliminada: " + rutaImagen);
                } catch (IOException e) {
                    System.err.println("Error al eliminar imagen: " + e.getMessage());
                }
            }

            // 2. Eliminar el espacio de la base de datos
            espacioRepo.deleteById(id);
        }
    }

    private Espacio toEntity(EspacioRequestDTO dto) {
        return Espacio.builder()
                .nombre(dto.getNombre())
                .capacidad(dto.getCapacidad())
                .ubicacion(dto.getUbicacion())
                .descripcion(dto.getDescripcion())
                .imagen(dto.getImagen())
                .tipoEspacio(tipoRepo.findById(dto.getIdTipoEspacio())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo espacio no válido")))
                .estado(estadoRepo.findById(dto.getIdEstado())
                        .orElseThrow(() -> new IllegalArgumentException("Estado no válido")))
                .equipamientos(
                        dto.getEquipamientos().stream()
                                .map(id -> equipamientoRepo.findById(id)
                                        .orElseThrow(() -> new IllegalArgumentException("Equipamiento no válido")))
                                .collect(Collectors.toList()))
                .build();
    }



    public EspacioResponseDTO toDTO(Espacio espacio) {
        EspacioResponseDTO dto = new EspacioResponseDTO();

        dto.setIdEspacio(espacio.getIdEspacio());
        dto.setNombre(espacio.getNombre());
        dto.setCapacidad(espacio.getCapacidad());
        dto.setUbicacion(espacio.getUbicacion());
        dto.setDescripcion(espacio.getDescripcion());
        dto.setImagen(espacio.getImagen());

        TipoEspacioDTO tipoDto = new TipoEspacioDTO();
        tipoDto.setIdTipoEspacio(espacio.getTipoEspacio().getIdTipoEspacio());
        tipoDto.setDescripcion(espacio.getTipoEspacio().getDescripcion());
        dto.setTipo(tipoDto);

        EstadoEspacioDTO estadoDto = new EstadoEspacioDTO();
        estadoDto.setIdEstado(espacio.getEstado().getIdEstado());
        estadoDto.setDescripcion(espacio.getEstado().getDescripcion());
        dto.setEstado(estadoDto);

        List<EquipamientoDTO> equipamientoDTOs = espacio.getEquipamientos().stream().map(eq -> {
            EquipamientoDTO e = new EquipamientoDTO();
            e.setIdEquipamiento(eq.getIdEquipamiento());
            e.setDescripcion(eq.getDescripcion());
            return e;
        }).toList();
        dto.setEquipamientos(equipamientoDTOs);

        return dto;
    }


}

