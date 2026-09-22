CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT'
        CHECK (role IN ('ADMIN', 'CLIENT'))
);

CREATE TABLE IF NOT EXISTS rooms (
    room_number VARCHAR(20) PRIMARY KEY,
    room_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    price_per_night NUMERIC(10, 2) NOT NULL
        CHECK (price_per_night >= 0)
);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY,
    reservation_code VARCHAR(50) NOT NULL UNIQUE,

    user_id UUID NOT NULL,
    room_number VARCHAR(20) NOT NULL,

    check_in DATE NOT NULL,
    check_out DATE NOT NULL,

    number_of_guests INTEGER NOT NULL
        CHECK (number_of_guests > 0),

    nights INTEGER NOT NULL
        CHECK (nights > 0),

    total_price NUMERIC(10, 2) NOT NULL
        CHECK (total_price >= 0),

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_client
        FOREIGN KEY (user_id)
        REFERENCES clients(id),

    CONSTRAINT fk_reservation_room
        FOREIGN KEY (room_number)
        REFERENCES rooms(room_number),

    CONSTRAINT check_reservation_dates
        CHECK (check_out > check_in)
);

CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY,

    reservation_id UUID NOT NULL UNIQUE,

    invoice_number VARCHAR(50) NOT NULL UNIQUE,

    subtotal_ht NUMERIC(10, 2) NOT NULL
        CHECK (subtotal_ht >= 0),

    vat_rate NUMERIC(5, 2) NOT NULL DEFAULT 20.00
        CHECK (vat_rate >= 0),

    vat_amount NUMERIC(10, 2) NOT NULL
        CHECK (vat_amount >= 0),

    total_ttc NUMERIC(10, 2) NOT NULL
        CHECK (total_ttc >= 0),

    issued_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,

    reservation_id UUID NOT NULL,

    amount NUMERIC(10, 2) NOT NULL
        CHECK (amount > 0),

    payment_date TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    status VARCHAR(30) NOT NULL,

    payment_method VARCHAR(30) NOT NULL,

    transaction_reference VARCHAR(100) UNIQUE,

    CONSTRAINT fk_payment_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
);