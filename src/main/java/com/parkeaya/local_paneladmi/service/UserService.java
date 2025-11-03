package com.parkeaya.local_paneladmi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkeaya.local_paneladmi.model.dto.UserDTO;
import com.parkeaya.local_paneladmi.model.entity.User;
import com.parkeaya.local_paneladmi.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Obtener todos los usuarios
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getNombre(),
                        u.getNombre(), // username temporal
                        u.getEmail(),
                        u.isStaff(),
                        u.isSuperuser()
                ))
                .collect(Collectors.toList());
    }

    // Obtener usuario por ID
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getNombre(),
                        u.getNombre(),
                        u.getEmail(),
                        u.isStaff(),
                        u.isSuperuser()
                ));
    }

    // Crear usuario
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setNombre(userDTO.getNombre());
        user.setEmail(userDTO.getEmail());
        user.setStaff(userDTO.isStaff());
        user.setSuperuser(userDTO.isSuperuser());

        User savedUser = userRepository.save(user);
        return new UserDTO(
                savedUser.getId(),
                savedUser.getNombre(),
                savedUser.getNombre(),
                savedUser.getEmail(),
                savedUser.isStaff(),
                savedUser.isSuperuser()
        );
    }

    // Actualizar usuario
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        user.setNombre(userDTO.getNombre());
        user.setEmail(userDTO.getEmail());
        user.setStaff(userDTO.isStaff());
        user.setSuperuser(userDTO.isSuperuser());

        User updatedUser = userRepository.save(user);
        return new UserDTO(
                updatedUser.getId(),
                updatedUser.getNombre(),
                updatedUser.getNombre(),
                updatedUser.getEmail(),
                updatedUser.isStaff(),
                updatedUser.isSuperuser()
        );
    }

    // Eliminar usuario
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
