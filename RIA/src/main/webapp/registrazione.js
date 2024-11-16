//Registration handler

window.addEventListener('load', function () {
    let myButton = document.getElementById("loginBtn");
    document.body.addEventListener("keydown", function (event) {
        if (event.key == 'Enter') {
            myButton.click();
        }
    });
}, false);

function checkSignup() {
    let errorMessage = document.getElementById("errorMsg");
    let email = document.getElementById("email").value;
    let username = document.getElementById("username").value;
    let password = document.getElementById("password1").value;
    let repeat = document.getElementById("password2").value;
    let xhttp = new XMLHttpRequest();

    if (email.length == 0 || username.length == 0 || password.length == 0 || repeat.length == 0) {
        errorMessage.innerHTML = "Non puoi lasciare campi vuoti";
        return;
    }

	if (email.length > 45 || username.length > 16 || password.length < 8 || password.length > 16
		|| repeat.length < 8 || repeat.length > 16) {
			errorMessage.innerHTML = "Lunghezza campi non rispettata";
        	return;
		}

    if (password != repeat) {
        errorMessage.innerHTML = "Password e ripeti password devono essere uguali";
        return;
    }

    if (!validateEmail(email)) {
        errorMessage.innerHTML = "Formato email non valido";
        return;
    }

    let postObj = {
        username: username,
        email: email,
        password1: password,
        password2: repeat
    }

    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                let user = xhttp.responseText;
                localStorage.setItem('utente', user.substring(0, user.length - 1));
                sessionStorage.setItem('utente', user.substring(0, user.length - 1));
                window.location.href = "home.html";
            } else {
                errorMessage.innerHTML = xhttp.responseText;
            }
        }
    }

    xhttp.open("POST", "ControllaRegistrazione");
    xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
    xhttp.send(JSON.stringify(postObj));

    return;
}

function validateEmail(email) {
    if (email.match(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)) {
        return true;
    } else {
        return false;
    }
}