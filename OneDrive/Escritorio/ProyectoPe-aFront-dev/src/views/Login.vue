<template>
  <div class="login-container d-flex flex-column align-items-center justify-content-center">
    <div class="card shadow p-4 login-card text-center">
      <!-- Logo centrado -->
      <div class="text-center mb-4 logo-container">
        <img src="@/assets/logo_peña-removebg-preview.png" alt="Logo Boca Juniors" class="logo" />
      </div>
      
      <form @submit.prevent="login" class="text-start">
        <div class="mb-4 input-group input-custom">
          <span class="input-group-text icon-bg">
            <i class="bi bi-envelope-fill"></i>
          </span>
          <input
            v-model="email"
            type="email"
            id="email"
            class="form-control form-control-lg input-field"
            placeholder="Correo electrónico"
            required
          />
        </div>

        <div class="mb-4 input-group input-custom">
          <span class="input-group-text icon-bg">
            <i class="bi bi-lock-fill"></i>
          </span>
          <input
            v-model="password"
            :type="passwordVisible ? 'text' : 'password'"
            id="password"
            class="form-control form-control-lg input-field"
            placeholder="Contraseña"
            required
          />
          <button
            type="button"
            class="btn btn-icon-toggle"
            @click="togglePasswordVisibility"
            :aria-label="passwordVisible ? 'Ocultar contraseña' : 'Mostrar contraseña'"
            tabindex="-1"
          >
            <i :class="passwordVisible ? 'bi bi-eye-fill' : 'bi bi-eye-slash-fill'"></i>
          </button>
        </div>

        <button type="submit" class="btn btn-login w-100 fw-bold" :disabled="loading">
          <span v-if="loading">INGRESANDO...</span>
          <span v-else>INGRESAR</span>
        </button>

        <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
        <div v-if="success" class="alert alert-success mt-3">{{ success }}</div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "LoginView",
  data() {
    return {
      email: "",
      password: "",
      passwordVisible: false,
      error: "",
      success: "",
      loading: false,
    };
  },
  methods: {
    togglePasswordVisibility() {
      this.passwordVisible = !this.passwordVisible;
    },
    async login() {
      this.error = "";
      this.success = "";
      this.loading = true;

      try {
        const response = await axios.post("http://127.0.0.1:8081/login", {
          email: this.email,
          password: this.password,
        });

        const data = response.data;
        this.success = data.message || "Login exitoso";

        if (data.token) {
          localStorage.setItem("token", data.token);
          this.$router.push("/principal");
        }
      } catch (e) {
        if (e.response) {
          this.error = e.response.data.error || "Error al iniciar sesión";
        } else {
          this.error = "No se pudo conectar con el servidor.";
        }
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
:root {
  --azul-boca: #03204c;
  --amarillo-boca: #ffd100;
  --amarillo-hover: #ffea00;
}

/* Contenedor general */
.login-container {
  position: relative;
  min-height: 100vh;
  background-image: url('@/assets/fondo.jpg');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center center;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* Capa oscura */
.login-container::before {
  content: "";
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.45);
  z-index: 0;
}

/* Tarjeta */
.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border: 4px solid var(--amarillo-boca);
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(3, 32, 76, 0.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: scale(1.06);
  box-shadow: 0 14px 40px rgba(3, 32, 76, 0.6);
}

/* Logo */
.logo-container {
  padding: 10px 0;
}

.logo {
  width: 110px;
  height: auto;
  filter: drop-shadow(0 0 3px rgba(3,32,76,0.8));
}

/* Inputs estilos modernos */
.input-custom {
  display: flex;
  align-items: center;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(3, 32, 76, 0.15);
  border: 1.5px solid transparent;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.input-custom:focus-within {
  border-color: var(--amarillo-boca);
  box-shadow: 0 0 8px var(--amarillo-boca);
}

.input-group-text.icon-bg {
  background-color: var(--azul-boca);
  color: var(--amarillo-boca);
  border: none;
  padding: 0 14px;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-field {
  border: none;
  border-radius: 0;
  padding: 14px 12px;
  font-size: 1.1rem;
  outline: none;
  box-shadow: none;
  flex-grow: 1;
  color: var(--azul-boca);
  font-weight: 500;
}

.input-field::placeholder {
  color: var(--azul-boca);
  opacity: 0.6;
}

/* Botón ojo */
.btn-icon-toggle {
  background: transparent;
  border: none;
  padding: 0 14px;
  font-size: 1.25rem;
  color: var(--azul-boca);
  cursor: pointer;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon-toggle:hover {
  color: var(--amarillo-boca);
}

.btn-icon-toggle:focus {
  outline: none;
  color: var(--amarillo-boca);
}

/* Botón ingresar */
.btn-login {
  background-color: var(--amarillo-boca);
  color: #000;
  border: none;
  font-size: 1.2rem;
  padding: 14px 0;
  border-radius: 12px;
  font-weight: bold;
  width: 100%;
  transition: all 0.3s ease;
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
}

.btn-login:hover{
  background-color: #03204c; /* Cambia completamente el fondo */
  color:#fff;                         /* Cambia el color del texto */
  box-shadow: 0 6px 15px #ffd100; /* Nueva sombra más profunda */
  transform: scale(1.05);             /* Agranda un poquito el botón */
}




.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

/* Mensajes */
.alert {
  font-weight: 600;
  font-size: 1rem;
  border-radius: 10px;
}
</style>
