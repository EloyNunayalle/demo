package org.example.proyecto.Rating;

import org.example.proyecto.Rating.Domain.Rating;
import org.example.proyecto.Rating.Domain.RatingService;
import org.example.proyecto.Rating.Infrastructure.RatingRepository;
import org.example.proyecto.Rating.dto.RatingRequestDto;
import org.example.proyecto.Rating.dto.RatingResponseDto;
import org.example.proyecto.Usuario.Domain.Usuario;
import org.example.proyecto.Usuario.infrastructure.UsuarioRepository;
import org.example.proyecto.exception.UnauthorizeOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearRatingExitosamente() {
        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setUsuarioId(1L);
        requestDto.setRaterUsuarioId(2L);
        requestDto.setRating(4);
        requestDto.setComment("Buen servicio");

        Usuario usuarioCalificado = new Usuario();
        usuarioCalificado.setId(1L);

        Usuario raterUsuario = new Usuario();
        raterUsuario.setId(2L);

        Rating rating = new Rating();
        rating.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioCalificado));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(raterUsuario));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        RatingResponseDto responseDto = new RatingResponseDto();
        responseDto.setId(1L);

        when(modelMapper.map(any(Rating.class), eq(RatingResponseDto.class))).thenReturn(responseDto);

        RatingResponseDto result = ratingService.crearRating(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    public void testObtenerRatingsPorUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        ratingService.obtenerRatingsPorUsuario(1L);

        verify(ratingRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    public void testDeleteRatingUnauthorized() {
        Rating rating = new Rating();
        rating.setId(1L);

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));

        assertThrows(UnauthorizeOperationException.class, () -> {
            ratingService.deleteItem(1L);
        });
    }
}
