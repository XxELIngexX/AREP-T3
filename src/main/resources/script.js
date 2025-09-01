document.addEventListener("DOMContentLoaded", () => {


    const form = document.getElementById("form-agregar-producto");
    const listaProductos = document.getElementById("productos-lista");
    const limpiarBtn = document.getElementById("limpiar");

    // Renderizar productos iniciales
    //renderListaProductos();

    // Manejar el envío del formulario
    form.addEventListener("submit", (e) => {
        e.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const precio = parseFloat(document.getElementById("precio").value.trim());
        const imagen = document.getElementById("imagen").value.trim();

        // Validar campos
        if (!nombre || isNaN(precio) || precio <= 0 || !imagen) {
            alert("Por favor, llena todos los campos correctamente.");
            return;
        }

        fetch("/product/add", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                nombre: nombre, precio: precio, imagen: imagen
            })
        })
            .then(res => res.json())
            .then(data => {

                console.log("respuesta:", data);
                renderListaProductos();

            })
            .catch(err => console.error("Error:", err));



        // Limpiar formulario
         form.reset();
    });

    // Manejar el botón de limpiar
    limpiarBtn.addEventListener("click", () => {
        form.reset();
    });

    async function getAllProducts() {
        try {
            const res = await fetch("/product/allProducts", {
                method: "GET",
                headers: {"Content-Type": "application/json"},
            });
            const data = await res.json();
            console.log("esto me llega de data:", data);
            return data;
        } catch (err) {
            console.error("Error en getAllProducts:", err);
            return [];
        }
    }

    // Función para renderizar la lista de productos
    async function renderListaProductos() {
        // Limpiar la lista actual
        listaProductos.innerHTML = "";
        let productos = await getAllProducts();

        console.log("desde el Renderizador")
        console.log(productos);
        // Renderizar cada producto de la lista
        productos.forEach((producto, index) => {
            const div = document.createElement("div");
            div.classList.add("producto");

            div.innerHTML = `
                <img src="${producto.imagen}" alt="${producto.nombre}">
                <h3>${producto.nombre}</h3>
                <p>${producto.precio}</p>
                <button class="eliminar" data-index="${index}">Eliminar</button>
            `;

            // Agregar funcionalidad al botón de eliminar
            div.querySelector(".eliminar").addEventListener("click", (e) => {
                const index = parseInt(e.target.getAttribute("data-index"), 10);
                eliminarProducto(index);
            });

            listaProductos.appendChild(div);
        });
    }

    // Función para eliminar un producto
    function eliminarProducto(index) {
        productos.splice(index, 1);
        renderListaProductos();
    }
});
