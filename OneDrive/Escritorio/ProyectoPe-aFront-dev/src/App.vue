<template>
  <div id="app">
    <!-- Mostrar NavBar en todas las rutas excepto en /login -->
    <NavBar v-if="route.path !== '/login'" />

    <!-- Si no está logueado, mostrar Login -->
    <LoginView v-if="!isAuthenticated && route.path === '/login'" @login-success="handleLoginSuccess" />

    <!-- Si está logueado, mostrar el contenido -->
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import LoginView from '@/views/Login.vue'

// Acceso a la ruta actual
const route = useRoute()
const router = useRouter()

// Estado de login
const isAuthenticated = ref(localStorage.getItem('logueado') === 'true')

// Si ya está logueado y está en /login, redirige automáticamente a /principal
watch(
  () => route.path,
  (newPath) => {
    if (isAuthenticated.value && newPath === '/login') {
      router.push('/principal')
    }
  },
  { immediate: true }
)

// Función al hacer login
const handleLoginSuccess = () => {
  isAuthenticated.value = true
  localStorage.setItem('logueado', 'true')
  router.push('/principal')
}
</script>
