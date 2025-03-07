package unibank.web.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unibank.web.app.dto.UserDto;
import unibank.web.app.entity.User;
import unibank.web.app.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    // Injected dependencies
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public User registerUser(UserDto userDto){
        User user = mapToUser(userDto);
        return userRepository.save(user);
    }

    public Map<String, Object> authenticateUser(UserDto userDto ){
        Map<String, Object> authObject = new HashMap<String, Object>();
        User user = (User) userDetailsService.loadUserByUsername(userDto.getUsername());
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userDto.getUsername(),
                userDto.getPassword()
        ));
        authObject.put("token", "Bearer".concat(jwtService.generateToken(userDto.getUsername())));
        authObject.put("user", user);
        return authObject;
    }

    private User mapToUser (UserDto userDto){
        return User.builder()
                .lastname(userDto.getLastname())
                .firstname(userDto.getFirstname())
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .tag("uni_" + userDto.getUsername())
                .dob(userDto.getDob())
                .roles(List.of("USER"))
                .build();
    }


}
