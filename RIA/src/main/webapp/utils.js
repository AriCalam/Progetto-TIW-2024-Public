/**
 * This method makes a call to the server.
 * @param method specifies if the method is GET or POST.
 * @param url specifies the url to call.
 * @param formElement specifies the form element to send.
 * @param callBack  specifies the function to call when the call is done
 * @param reset specifies if the form should be reset after the call.
 */
function makeCall(method, url, callBack, formElement, reset = true) {
	const request = new XMLHttpRequest();
	
	request.onreadystatechange = function() {
		callBack(request);
	}
	
	request.open(method, url);
	if(formElement != null)
		request.send(new FormData(formElement)); //POST
	else
		request.send(); //GET
    
    if (formElement != null && reset)
        formElement.reset();
}

/**
 * This method makes a call to the server. Version 2, deprecated.
 * @param method specifies if the method is GET or POST.
 * @param url specifies the url to call.
 * @param formElement specifies the form element to send.
 * @param callBack  specifies the function to call when the call is done
 * @param reset specifies if the form should be reset after the call.
 */
function makeCall2(method, url, callBack, username, pass1, pass2, mail, formElement, reset = true) {
	const request = new XMLHttpRequest();
	
	request.onreadystatechange = function() {
		callBack(request);
	}
	
	request.open(method, url);
	request.setRequestHeader("username", username);
	request.setRequestHeader("password1", pass1);
	request.setRequestHeader("password2", pass2);
	request.setRequestHeader("email", mail);
	//console.log(formElement); //For debugging
	if(formElement != null)
		request.send(new FormData(formElement)); //POST
	else
		request.send(); //GET
}

/**
 * This method sends a form data to the server.
 * @param method specifies if the method is GET or POST.
 * @param url specifies the url to call.
 * @param callBack specifies the function to call when the call is done
 * @param formData specifies the form data to send.
 */
function sendFormData(method, url, callBack, formData) {
    const request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        callBack(request);
    };
    request.open(method, url);
    if (formData != null) {
        request.send(formData);
    } else {
        request.send();
    }
}