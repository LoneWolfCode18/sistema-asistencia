-- Script SQL para insertar datos de prueba en la base de datos de Supabase
-- Tabla: usuarios

INSERT INTO usuarios (nombre, correo, telegram_user, estado, fecha_creacion)
VALUES (
    'Shady Developer', 
    'shady.dev@dn-software.com', 
    'shady_dev', 
    'ACTIVO', 
    CURRENT_TIMESTAMP
)
ON CONFLICT (correo) 
DO UPDATE SET 
    nombre = EXCLUDED.nombre,
    telegram_user = EXCLUDED.telegram_user,
    estado = EXCLUDED.estado;
