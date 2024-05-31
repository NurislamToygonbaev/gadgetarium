CREATE SEQUENCE banner_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE brand_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE category_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE values_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE contact_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE discount_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE email_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE feedback_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE gadget_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE mailing_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE order_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE password_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE sub_category_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE sub_gadget_seq START WITH 60 INCREMENT BY 1;
CREATE SEQUENCE user_seq START WITH 60 INCREMENT BY 1;

CREATE TABLE banner (
                        id BIGINT PRIMARY KEY,
                        images TEXT[]
);

CREATE TABLE brand (
                       id BIGINT PRIMARY KEY,
                       brand_name VARCHAR(255),
                       logo VARCHAR(1000)
);

CREATE TABLE category (
                          id BIGINT PRIMARY KEY,
                          category_name VARCHAR(255)

);

CREATE TABLE char_value (
                            id BIGINT PRIMARY KEY
);

CREATE TABLE char_value_values (
                                   char_value_id BIGINT REFERENCES char_value(id),
                                   key TEXT,
                                   value TEXT,
                                   PRIMARY KEY (char_value_id, key)
);

CREATE TABLE contact (
                         id BIGINT PRIMARY KEY,
                         firstname VARCHAR(255),
                         lastname VARCHAR(255),
                         email VARCHAR(255),
                         phone_number VARCHAR(255),
                         message VARCHAR(1000)
);

CREATE TABLE discount (
                          id BIGINT PRIMARY KEY,
                          percent INT,
                          start_date DATE,
                          end_date DATE,
                          gadget_id BIGINT REFERENCES gadget(id)
);

CREATE TABLE email_address (
                               id BIGINT PRIMARY KEY,
                               email VARCHAR(255)
);

CREATE TABLE feedback (
                          id BIGINT PRIMARY KEY,
                          rating INT,
                          description VARCHAR(500),
                          date_and_time TIMESTAMP,
                          response_admin VARCHAR(500),
                          review_type VARCHAR(255),
                          images TEXT[],
                          gadget_id BIGINT REFERENCES gadget(id),
                          user_id BIGINT REFERENCES "user"(id)
);

CREATE TABLE gadget (
                        id BIGINT PRIMARY KEY,
                        warranty INT,
                        release_date DATE,
                        video_url VARCHAR(500),
                        pdf_url VARCHAR(500),
                        description VARCHAR(1000),
                        name_of_gadget VARCHAR(255),
                        rating DOUBLE PRECISION,
                        created_at DATE,
                        sub_category_id BIGINT REFERENCES sub_category(id),
                        brand_id BIGINT REFERENCES brand(id),
                        discount_id BIGINT REFERENCES discount(id)
);

CREATE TABLE gadget_char_values (
                                    gadget_id BIGINT REFERENCES gadget(id),
                                    char_value_id BIGINT REFERENCES char_value(id),
                                    value TEXT,
                                    PRIMARY KEY (gadget_id, char_value_id)
);

CREATE TABLE mailing (
                         id BIGINT PRIMARY KEY,
                         image VARCHAR(1000),
                         title VARCHAR(255),
                         description VARCHAR(500),
                         start_date DATE,
                         end_date DATE
);

CREATE TABLE "order" (
                         id BIGINT PRIMARY KEY,
                         number BIGINT,
                         type_order BOOLEAN,
                         created_at DATE,
                         total_price DECIMAL,
                         discount_price DECIMAL,
                         payment VARCHAR(255),
                         status VARCHAR(255),
                         user_id BIGINT REFERENCES "user"(id)
);

CREATE TABLE order_sub_gadgets (
                                   order_id BIGINT REFERENCES "order"(id),
                                   sub_gadget_id BIGINT REFERENCES sub_gadget(id),
                                   PRIMARY KEY (order_id, sub_gadget_id)
);

CREATE TABLE password_reset_token (
                                      id BIGINT PRIMARY KEY,
                                      token VARCHAR(255),
                                      expiry_date TIMESTAMP,
                                      user_id BIGINT REFERENCES "user"(id)
);

CREATE TABLE sub_category (
                              id BIGINT PRIMARY KEY,
                              sub_category_name VARCHAR(255),
                              category_id BIGINT REFERENCES category(id)
);

CREATE TABLE sub_gadget (
                            id BIGINT PRIMARY KEY,
                            quantity INT,
                            main_colour VARCHAR(255),
                            count_sim INT,
                            price DECIMAL,
                            article BIGINT,
                            memory VARCHAR(255),
                            ram VARCHAR(255),
                            remoteness_status VARCHAR(255),
                            images TEXT[],
                            uni_filed TEXT[],
                            gadget_id BIGINT REFERENCES gadget(id)
);

CREATE TABLE sub_gadget_orders (
                                   sub_gadget_id BIGINT REFERENCES sub_gadget(id),
                                   order_id BIGINT REFERENCES "order"(id),
                                   PRIMARY KEY (sub_gadget_id, order_id)
);

CREATE TABLE "user" (
                        id BIGINT PRIMARY KEY,
                        first_name VARCHAR(255),
                        last_name VARCHAR(255),
                        image VARCHAR(1000),
                        phone_number VARCHAR(255),
                        email VARCHAR(255),
                        password VARCHAR(255),
                        address VARCHAR(255),
                        role VARCHAR(255)
);

CREATE TABLE user_orders (
                             user_id BIGINT REFERENCES "user"(id),
                             order_id BIGINT REFERENCES "order"(id),
                             PRIMARY KEY (user_id, order_id)
);

CREATE TABLE user_feedbacks (
                                user_id BIGINT REFERENCES "user"(id),
                                feedback_id BIGINT REFERENCES feedback(id),
                                PRIMARY KEY (user_id, feedback_id)
);

CREATE TABLE user_basket (
                             user_id BIGINT REFERENCES "user"(id),
                             sub_gadget_id BIGINT REFERENCES sub_gadget(id),
                             quantity INT,
                             PRIMARY KEY (user_id, sub_gadget_id)
);

CREATE TABLE user_comparison (
                                 user_id BIGINT REFERENCES "user"(id),
                                 sub_gadget_id BIGINT REFERENCES sub_gadget(id),
                                 PRIMARY KEY (user_id, sub_gadget_id)
);

CREATE TABLE user_viewed (
                             user_id BIGINT REFERENCES "user"(id),
                             sub_gadget_id BIGINT REFERENCES sub_gadget(id),
                             PRIMARY KEY (user_id, sub_gadget_id)
);

CREATE TABLE user_likes (
                            user_id BIGINT REFERENCES "user"(id),
                            sub_gadget_id BIGINT REFERENCES sub_gadget(id),
                            PRIMARY KEY (user_id, sub_gadget_id)
);

CREATE TABLE user_password_reset_tokens (
                                            user_id BIGINT REFERENCES "user"(id),
                                            token_id BIGINT REFERENCES password_reset_token(id),
                                            PRIMARY KEY (user_id, token_id)
);
