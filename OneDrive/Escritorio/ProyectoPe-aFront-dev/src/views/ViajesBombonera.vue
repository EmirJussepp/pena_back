<template>
  <div class="container mt-5 text-dark">
    <div class="page-header mb-4">
      <h1>
        <i></i>
        Crear Viaje a la Bombonera
      </h1>
    </div>

    <div class="card shadow-lg p-4">
      <form @submit.prevent="crearViaje">
        <div class="mb-3">
          <label class="form-label fw-semibold">Fecha del viaje</label>
          <input
            type="datetime-local"
            v-model="viaje.fechaViaje"
            class="form-control"
            required
          />
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">Destino</label>
          <input
            type="text"
            v-model="viaje.destino"
            class="form-control"
            required
          />
        </div>

        <div class="text-end">
          <button type="submit" class="btn-boca-primary">
            <i class="bi bi-check-circle me-2"></i> Crear Viaje
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'

const viaje = ref({
  fechaViaje: '',
  destino: '',
})

const crearViaje = async () => {
  try {
    await axios.post('http://127.0.0.1:8081/viajesBombonera', viaje.value)

    Swal.fire({
      icon: 'success',
      title: 'Viaje creado',
      text: 'El viaje fue creado exitosamente.',
      confirmButtonColor: '#003366',
    })

    // Limpia el formulario si querés
    viaje.value.fechaViaje = ''
    viaje.value.destino = ''
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Error al crear el viaje',
      text: error.response?.data?.message || 'Ocurrió un error inesperado.',
      confirmButtonColor: '#dc3545',
    })
  }
}
</script>


<style scoped>
.page-header h1 {
  font-size: 2.4rem;
  font-weight: 900;
  color: #003366;
  text-align: center;
  letter-spacing: 0.04em;
  user-select: none;
  position: relative;
}

.page-header h1::after {
  content: "";
  display: block;
  width: 140px;
  height: 4px;
  background: #ffc107;
  margin: 0.25rem auto 0;
  border-radius: 2px;
}

.btn-boca-primary {
  background-color: #003366;
  color: #ffc107;
  border: none;
  border-radius: 0.5rem;
  padding: 0.55rem 1.25rem;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease, color 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-boca-primary:hover,
.btn-boca-primary:focus {
  background-color: #00509e;
  color: #fff;
  outline: none;
}

input.form-control {
  border-radius: 0.375rem;
  font-size: 1rem;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.1);
  transition: box-shadow 0.3s ease;
}

input.form-control:focus {
  box-shadow: 0 0 8px #00509e;
  border-color: #00509e;
  outline: none;
}
</style>
