//Login handler

window.addEventListener('load', function () {
    let myButton = document.getElementById("loginBtn");
    document.body.addEventListener("keydown", function (event) {
        if (event.key == 'Enter') {
            myButton.click();
        }
    });
}, false);

function checkLogin() {
    let errorMessage = document.getElementById("errorMsg");
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;

    if (username.length == 0 || password.length == 0) {
        errorMessage.innerHTML = "Non puoi lasciare dei campi vuoti";
        return;
    }
    else if (username.length > 16 || password.length > 16 || password.length < 8) {
		errorMessage.innerHTML = "Lunghezza campi non rispettata";
        return;
	}
	
    let xhttp = new XMLHttpRequest();

    let postObj = {
        username: username,
        password: password,
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

    xhttp.open("POST", "ControllaLogin");
    xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
    xhttp.send(JSON.stringify(postObj));

    return;
}