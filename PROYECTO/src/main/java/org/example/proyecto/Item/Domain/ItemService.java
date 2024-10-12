package org.example.proyecto.Item.Domain;

import org.example.proyecto.Category.Domain.Category;
import org.example.proyecto.Category.Infrastructure.CategoryRepository;
import org.example.proyecto.Item.Infraestructure.ItemRepository;
import org.example.proyecto.Item.dto.ItemRequestDto;
import org.example.proyecto.Item.dto.ItemResponseDto;
import org.example.proyecto.Usuario.Domain.Usuario;
import org.example.proyecto.Usuario.infrastructure.UsuarioRepository;
import org.example.proyecto.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository, UsuarioRepository usuarioRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ItemResponseDto createItem(ItemRequestDto itemDto) {
        Category category = categoryRepository.findById(itemDto.getCategory_id())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrado"));

        Usuario user = usuarioRepository.findById(itemDto.getUser_id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        //mapeamos de itemDto a item
        Item item = modelMapper.map(itemDto, Item.class);

        // Agregamos la categoria y user que no estaban en el dto
        item.setCategory(category);
        item.setUsuario(user);

        Item savedItem = itemRepository.save(item);

        //mapeamos la entidad guardada a un response y luego la retornamos
        ItemResponseDto responseDto= modelMapper.map(savedItem, ItemResponseDto.class);

        responseDto.setUserName(item.getUsuario().getEmail());
        responseDto.setCategoryName(item.getCategory().getName());

        return responseDto;
    }

    public ItemResponseDto updateItem(Long itemId, ItemRequestDto itemRequestDto) {

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));


        Category category = categoryRepository.findById(itemRequestDto.getCategory_id())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Usuario usuario = usuarioRepository.findById(itemRequestDto.getUser_id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));


        modelMapper.map(itemRequestDto, existingItem);

        // Establecer manualmente la categoría y el usuario si no están en el DTO
        existingItem.setCategory(category);
        existingItem.setUsuario(usuario);

        Item updatedItem = itemRepository.save(existingItem);

        ItemResponseDto responseDto= modelMapper.map(updatedItem, ItemResponseDto.class);

        responseDto.setUserName(updatedItem.getUsuario().getEmail());
        responseDto.setCategoryName(updatedItem.getCategory().getName());

        return responseDto;
    }

    public ItemResponseDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        return modelMapper.map(item, ItemResponseDto.class);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));
        itemRepository.delete(item);
    }

    public List<ItemResponseDto> getAllItems() {
        List<Item> items = itemRepository.findAll();

        // Mapear cada Item a ItemResponseDto
        return items.stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> getItemsByCategory(Long categoryId) {
        List<Item> items = itemRepository.findByCategoryId(categoryId);
        return items.stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }


    public List<ItemResponseDto> getItemsByUser(Long userId) {
        List<Item> items = itemRepository.findByUsuarioId(userId);
        return items.stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

}
