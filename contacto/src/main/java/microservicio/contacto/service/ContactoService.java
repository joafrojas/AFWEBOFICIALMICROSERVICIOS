package microservicio.contacto.service;

import microservicio.contacto.model.Contacto;
import microservicio.contacto.repository.ContactoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ContactoService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private ContactoRepository contactoRepository;

    public Contacto guardarContacto(Contacto contacto) {
        if (contacto.getNombre() == null || contacto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (contacto.getEmail() == null || !EMAIL_PATTERN.matcher(contacto.getEmail()).matches()) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (contacto.getMensaje() == null || contacto.getMensaje().trim().length() < 10) {
            throw new IllegalArgumentException("Mensaje muy corto (mínimo 10 caracteres)");
        }
        return contactoRepository.save(contacto);
    }

    public Iterable<Contacto> listarContactos() {
        return contactoRepository.findAll();
    }

    public void eliminarContacto(Long id) {
        if (!contactoRepository.existsById(id)) {
            throw new IllegalArgumentException("Contacto no encontrado");
        }
        contactoRepository.deleteById(id);
    }
}
