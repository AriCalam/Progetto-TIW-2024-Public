{ //Evita che le variabili finiscano nello scope globale se non serve
	let treeDiv, creaSottocartella, creaDocumento, creaCartella, pageOrchestrator = new PageOrchestrator();
	//let opened_modal = null;

	window.addEventListener("load", () => {

		if (localStorage.getItem("utente") == null)
			window.location.href = "login.html";
		else {
			pageOrchestrator.start(); //Inizializza i componenti
			pageOrchestrator.refresh();
		}

		pageOrchestrator.start();
		pageOrchestrator.refresh();
	}, false);

	/*
	window.onclick = function (event) {
		if (event.target == opened_modal)
			opened_modal.style.display = "none";
	}*/

	function checkResponse(response) {
		if (response.readyState === XMLHttpRequest.DONE) {
			let text = response.responseText;
			switch (response.status) {
				case 200:
					pageOrchestrator.refresh();
					break;
				case 401:
					alert("Non sei loggato. Fai il login.");
					let loggedOut = false;
					makeCall("GET", "Logout", function (response) {
						if (response.readyState === XMLHttpRequest.DONE) {
							switch (response.status) {
								case 200:
									loggedOut = true;
									localStorage.clear();
									sessionStorage.clear();
									window.location.href = "login.html";
									break;
								default:
									alert("Errore!");
							}
						}
					});
					if (!loggedOut) {
						localStorage.clear();
						sessionStorage.clear();
						window.location.href = "login.html";
					}
					break;
				case 400:
				case 500:
					alert(text);
					break;
				default:
					alert("Unknown error");
			}
		}
	}

	//Costruttore dei componenti della view
	function Tree(container) {
		this.container = container;

		this.reset = () => {
			this.show();
		}

		this.show = () => {
			var self = this;
			makeCall("POST", "GoToHome", (req) => {
				if (req.readyState == 4) {
					var msg = req.responseText;
					if (req.status == 200) {
						let treeObject = JSON.parse(msg);
						self.update(treeObject);
					} else {
						alert(msg);
					}
				}
			}, null);
		}

		//obj è la lista di cartelle con idPadre==null: per ogni elemento di questa lista obj, chiamo una funzione ricorsiva
		this.update = (obj) => {
			this.container.innerHTML = "";
			var rootList = document.createElement("ul");

			obj.forEach((o) =>
				this.rec(o, rootList)
			);

			//Creazione della cartella Cestino
			let cestinoLi = document.createElement("p");
			let cestinoDiv = document.createElement("div");
			cestinoDiv.textContent = "Cestino";
			cestinoDiv.id = "cestino";
			cestinoDiv.classList.add("cestino");
			cestinoLi.append(cestinoDiv);
			cestinoLi.ondrop = dropBin;
			cestinoLi.ondragover = allowDrop;
			rootList.appendChild(cestinoLi);

			this.container.appendChild(rootList);
		}

		//Funzione ricorsiva
		this.rec = (nodo, lista) => {
			let element = document.createElement("ul");
			if (nodo.sottocartelle.length != 0) {
				nodo.sottocartelle.forEach(o => this.rec(o, element));
			}

			let li = document.createElement("li");
			let divCartella = document.createElement("div");
			divCartella.classList.add("nomeElemento");
			divCartella.id = "cartella_" + nodo.idCartella;
			divCartella.textContent = nodo.nomeCartella + " ";
			divCartella.draggable = true;
			divCartella.ondragstart = drag;
			divCartella.ondrop = dropFolder;
			divCartella.ondragover = allowDrop;
			li.append(divCartella);

			//Bottone per vedere i contenuti della cartella
			let contenuti_btn = document.createElement("button");
			contenuti_btn.className = "cartella-btn";
			contenuti_btn.textContent = "Contenuti";
			contenuti_btn.id = "contenuti_btn";
			contenuti_btn.classList.add("contenuti-view-btn");
			li.append(contenuti_btn);
			contenuti_btn.addEventListener("click", function () {
				contenutiCartella.mostraContenuti(nodo.idCartella, document.getElementById("contenitoreDestra"));
			});

			//Bottone per creare una nuova sottocartella nella cartella
			let sottoc_btn = document.createElement("button");
			sottoc_btn.className = "cartella_btn";
			sottoc_btn.textContent = "+ Sottocartella";
			sottoc_btn.id = "sottoc_btn";
			sottoc_btn.classList.add("sottoc-add-btn");
			li.append(sottoc_btn);
			sottoc_btn.addEventListener("click", function () {
				creaSottocartella.enableForm(nodo.idCartella, nodo.nomeCartella);
			});

			//Bottone per creare un nuovo documento nella cartella
			let doc_btn = document.createElement("button");
			doc_btn.className = "cartella-btn";
			doc_btn.textContent = "+ Documento";
			doc_btn.id = "doc_btn";
			doc_btn.classList.add("doc-add-btn");
			li.append(doc_btn);
			doc_btn.addEventListener("click", function () {
				creaDocumento.enableForm(nodo.idCartella, nodo.nomeCartella);
			});

			li.appendChild(element);
			lista.appendChild(li);
		}
	}

	function MostraContenuti(container) {

		this.hide = function () {
			let contenutiCartella2 = document.getElementById("contenutiCartella");
			let contenitoreDestra = document.getElementById("contCentro");
			if (contenitoreDestra.contains(contenutiCartella2))
				contenitoreDestra.removeChild(contenutiCartella2);
		}

		//Metodo che crea la lista di cartelle e prepara il loro contenuto
		this.mostraContenuti = function (idCartella, container) {
			let self = this;
			let contenutiCartella = document.createElement("div");
			contenutiCartella.setAttribute("id", "contenutiCartella");
			contenutiCartella.classList.add("contenutiCartella");
			makeCall("GET", "GoToContenuti?idCartella=" + idCartella, function (response) {
				if (response.readyState === XMLHttpRequest.DONE) {
					let message = response.responseText;
					switch (response.status) {
						case 200: //OK
							self.setContenutiCartella(JSON.parse(message), contenutiCartella);
							break;
						case 400: //Bad Request
							//document.getElementById("errorMsg").textContent = message;
							alert(message);
							break;
						case 401: //Unauthorized
							//document.getElementById("errorMsg").textContent = message;
							alert(message);
							window.location.href = "login.html";
							break;
						case 403: //Forbidden
							//document.getElementById("errorMsg").textContent = message;
							alert(message);
							break;
						case 500: //Internal server error
							alert(message);
							break;
						default:
							alert("Unknown error: " + "\n" + message);
					}
				}
			});
		}

		this.setContenutiCartella = function (cartella, container) {
			let self = this;
			pageOrchestrator.hideContent();
			document.getElementById("contCentro").style.visibility = "visible";

			//Aggiungo la lista delle sottocartelle
			if (cartella.cartelle != null) {
				let st = document.createElement("h4");
				st.textContent = "Sottocartelle di " + cartella.nomeCartella;
				st.classList.add("panel-title");
				container.appendChild(st);

				let rootList = document.createElement("ul");
				let sottocartList = cartella.cartelle;

				//Creo la lista di sottocartelle della cartella
				sottocartList.forEach(o => {
					let li = document.createElement("li");
					let cDiv = document.createElement("div");
					cDiv.classList.add("nomeElemento");
					cDiv.textContent = o.nomeCartella;
					li.setAttribute("id", "cartella" + o.idCartella);
					li.append(cDiv);

					//Bottone per vedere i contenuti della cartella
					let contenuti_btn = document.createElement("button");
					contenuti_btn.className = "cartella-btn";
					contenuti_btn.id = "contenuti_btn";
					contenuti_btn.textContent = "Mostra contenuti";
					contenuti_btn.classList.add("contenuti-view-btn");
					li.append(contenuti_btn);
					contenuti_btn.addEventListener("click", function () {
						self.mostraContenuti(o.idCartella, container);
					});

					rootList.appendChild(li);
				});

				container.appendChild(rootList);
			}

			//Aggiungo la lista dei documenti
			if (cartella.documenti.length != null) {
				let dt = document.createElement("h4");
				dt.textContent = "Documenti di " + cartella.nomeCartella;
				dt.classList.add("panel-title");
				container.appendChild(dt);

				let rootList2 = document.createElement("ul");
				let docList = cartella.documenti;

				docList.forEach(o => {
					let li = document.createElement("li");
					let cDiv = document.createElement("div");
					cDiv.textContent = o.nomeDocumento;
					cDiv.draggable = true;
					cDiv.ondragstart = drag;
					cDiv.id = "documento_" + o.idDocumento;
					li.append(cDiv);

					//Bottone per vedere i dettagli del documento
					let dettagli_btn = document.createElement("button");
					dettagli_btn.className = "cartella-btn";
					dettagli_btn.textContent = "Mostra dettagli";
					dettagli_btn.classList.add("contenuti-view-btn");
					li.append(dettagli_btn);
					dettagli_btn.addEventListener("click", function () {
						self.mostraDettagliDoc(o.idDocumento, cartella.nomeCartella, container);
					});

					rootList2.appendChild(li);
				});

				container.appendChild(rootList2);
			}

			//Aggiungo il bottone per nascondere il contenuto della cartella
			let divBtn = document.createElement("div");
			divBtn.classList.add("centerCol-hide-btn-div");
			let btn = document.createElement("button");
			btn.id = "hideContenutoCartellaBtn";
			btn.className = "cartella-btn";
			btn.classList.add("hide-btn-center");
			btn.textContent = "Nascondi contenuto cartella"
			btn.addEventListener("click", function () {
				self.hide();
			});

			divBtn.appendChild(btn);
			container.appendChild(divBtn);

			document.getElementById("contCentro").appendChild(container);
		}

		this.mostraDettagliDoc = function (idDocumento, nomeCartella, container) {
			let self = this;
			let contenutiDoc = document.createElement("div");
			contenutiDoc.setAttribute("id", "contenutiDoc");
			contenutiDoc.classList.add("contenutiCartella");
			makeCall("GET", "DettagliDoc?idDocumento=" + idDocumento, function (response) {
				if (response.readyState === XMLHttpRequest.DONE) {
					let message = response.responseText;
					switch (response.status) {
						case 200: //OK
							self.setContenutiDoc(JSON.parse(message), nomeCartella, contenutiDoc);
							break;
						case 400: //Bad Request
							document.getElementById("errorMsg").textContent = message;
							break;
						case 401: //Unauthorized
							document.getElementById("errorMsg").textContent = message;
							break;
						case 403: //Forbidden
							document.getElementById("errorMsg").textContent = message;
							break;
						case 500: //Internal server error
							alert(message);
							break;
						default:
							alert("Unknown error: " + "\n" + message);
					}
				}
			});
		}

		this.setContenutiDoc = function (documento, nomeCartella, container) {
			let self = this;
			self.hideDoc();

			let t = document.createElement("h4");
			t.textContent = "Documento " + documento.nomeDocumento + documento.tipo;
			container.appendChild(t);

			let nomeD = document.createElement("p");
			nomeD.textContent = "Nome: " + documento.nomeDocumento;
			container.appendChild(nomeD);

			let tipoD = document.createElement("p");
			tipoD.textContent = "Tipo: " + documento.tipo;
			container.appendChild(tipoD);

			let padre = document.createElement("p");
			padre.textContent = "Cartella padre: " + nomeCartella;
			container.appendChild(padre);

			let dataD = document.createElement("p");
			dataD.textContent = "Data creazione: " + documento.dataCreazione;
			container.appendChild(dataD);

			let somm = document.createElement("p");
			somm.textContent = "Sommario: " + documento.sommario;
			container.appendChild(somm);

			let divBtn = document.createElement("div");
			divBtn.classList.add("centerCol-hide-btn-div");
			let btn = document.createElement("button");
			btn.id = "hideDettagliDocBtn";
			btn.className = "cartella-btn";
			//btn.classList.add("centerCol-hide-btn");
			btn.classList.add("hide-btn-center");
			btn.textContent = "Nascondi dettagli documento"
			btn.addEventListener("click", function () {
				self.hideDoc();
			});

			divBtn.appendChild(btn);
			container.appendChild(divBtn);

			document.getElementById("contCentro").appendChild(container);
		}

		this.hideDoc = function () {
			let dett = document.getElementById("contenutiDoc");
			let contenitoreDestra = document.getElementById("contCentro");
			//contenitoreDestra.style.visibility = "hidden";
			if (contenitoreDestra.contains(dett))
				contenitoreDestra.removeChild(dett);
		}
	}

	//Classe che gestisce la creazione di una sottocartella
	function CreaSottocartella(container, button) {
		this.button = button;
		document.getElementById("creaSottocartella").style.visibility = "hidden";
		let self = this;
		const title = document.getElementById("createSubFolderTitle");
		const form = document.getElementById("creaSottocartella");

		form.addEventListener("submit", function (e) {
			e.preventDefault();
			document.getElementById("okMsgSC").textContent = "";
			document.getElementById("errorMsgSC").textContent = "";
			if (form.checkValidity()) {
				makeCall("POST", 'CreaSottocartella', function (response) {
					if (response.readyState === XMLHttpRequest.DONE) {
						const message = response.responseText;
						switch (response.status) {
							case 200: //Ok
								document.getElementById("okMsgSC").textContent = message;
								pageOrchestrator.refresh();
								break;
							case 400: //Bad Request
							case 401: //Unauthorized
							case 403: //Forbidden
							case 409: //Conflict
							case 500: //Internal server error
								document.getElementById("errorMsgSC").textContent = message;
								break;
							default:
								alert("Unknown error: " + "\n" + message);
						}
					}
				}, form);
				form.reset();
			} else form.reportValidity();
		}, false);

		//Metodo per nascondere il contenitore
		this.hide = function () {
			container.style.visibility = "hidden";
			document.getElementById("okMsgSC").textContent = "";
			document.getElementById("errorMsgSC").textContent = "";
			document.getElementById("creaSottocartella").style.visibility = "hidden";
			document.getElementById("creaSottocartella").reset();
		}

		//Metodo per settare il form di creazione visibile
		this.enableForm = function (folderId, folderName) {
			//pageOrchestrator.hideContent();
			container.style.visibility = "visible";
			document.getElementById("creaSottocartella").style.visibility = "visible";
			form.getElementsByClassName("hiddenInput")[0].value = folderId;
			title.textContent = "Crea una sottocartella nella cartella " + folderName;
			container.append(form);
			let hbtn = document.getElementById("hideSC");
			hbtn.addEventListener("click", function () {
				self.hide();
			});
		}
	}

	//Funzione per creare effettivamente un documento
	function creaFile() {
		let xhttp = new XMLHttpRequest();
		let nomeDoc = document.getElementById("nomeDocumento").value;
		let tipo = document.getElementById("tipo").value;
		let sommario = document.getElementById("sommario").value;
		let idPadre = document.getElementById("idPadreDoc").value;

		if (nomeDoc.length == 0 || tipo.length == 0 || sommario.length == 0 || nomeDoc.length > 16 || tipo.length > 5 || sommario.length > 140)
			return;

		let postObj = {
			nomeDocumento: nomeDoc,
			tipo: tipo,
			sommario: sommario,
			idPadre: idPadre
		}

		xhttp.onload = function () {
			if (xhttp.readyState == XMLHttpRequest.DONE) {
				document.getElementById("okMsgD").textContent = "";
				document.getElementById("errorMsgD").textContent = "";
				switch (xhttp.status) {
					case 200: //Ok
						document.getElementById("creaDocumento").reset();
						document.getElementById("okMsgD").textContent = xhttp.responseText;
						document.getElementById("hideContenutoCartellaBtn").click();
						pageOrchestrator.refresh();
						break;
					case 400: //Bad Request
					case 401: //Unauthorized
					case 403: //Forbidden
					case 409: //Conflict
					case 500: //Internal server error 		
						document.getElementById("errorMsgD").textContent = xhttp.responseText;
						break;
					default:
						alert(xhttp.responseText);
						break;
				}
			}
		}

		xhttp.open("POST", "CreaDoc");
		xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
		xhttp.send(JSON.stringify(postObj));
		document.getElementById("creaDocumento").reset();
	}

	//Classe che gestisce la creazione di una cartella
	function CreaCartella(container, button) {
		this.button = button;
		document.getElementById("creaCartella").style.visibility = "hidden";
		let self = this;
		const title = document.getElementById("createFolderTitle");
		const form = document.getElementById("creaCartella");

		form.addEventListener("submit", function (e) {
			e.preventDefault();
			if (form.checkValidity()) {
				makeCall("POST", 'CreaCartella', function (response) {
					if (response.readyState === XMLHttpRequest.DONE) {
						document.getElementById("okMsgC").textContent = "";
						document.getElementById("errorMsgC").textContent = "";
						switch (response.status) {
							case 200: //Ok
								document.getElementById("okMsgC").textContent = response.responseText;
								pageOrchestrator.refresh();
								break;
							case 400: //Bad Request
							case 401: //Unauthorized
							case 403: //Forbidden
							case 409: //Conflict
							case 500: //Internal server error 		
								document.getElementById("errorMsgC").textContent = response.responseText;
								break;
							default:
								alert(response.responseText);
								break;
						}
					}
				}, form);
				form.reset();
			} else form.reportValidity();
		}, false);

		//Metodo per nascondere il contenitore
		this.hide = function () {
			container.style.visibility = "hidden";
			document.getElementById("errorMsgC").textContent = "";
			document.getElementById("okMsgC").textContent = "";
			document.getElementById("creaCartella").style.visibility = "hidden";
			document.getElementById("creaCartella").reset();
		}

		//Metodo per settare il form di creazione visibile
		this.enableForm = function () {
			document.getElementById("errorMsgC").textContent = "";
			document.getElementById("okMsgC").textContent = "";
			container.style.visibility = "visible";
			document.getElementById("creaCartella").style.visibility = "visible";
			title.textContent = "Crea una cartella";
			container.append(form);
			let hbtn = document.getElementById("hideC");
			hbtn.addEventListener("click", function () {
				self.hide();
			});
		}
	}

	//Classe che gestisce la creazione di un documento
	function CreaDocumento(container, button) {
		this.button = button;
		document.getElementById("creaDocumento").style.visibility = "hidden";
		let self = this;
		const title = document.getElementById("creaDocTitle");
		const form = document.getElementById("creaDocumento");

		//Metodo per nascondere il contenitore
		this.hide = function () {
			container.style.visibility = "hidden";
			document.getElementById("errorMsgD").textContent = "";
			document.getElementById("okMsgD").textContent = "";
			document.getElementById("creaDocumento").style.visibility = "hidden";
			document.getElementById("creaDocumento").reset();
		}

		//Metodo per settare il form di creazione visibile
		this.enableForm = function (folderId, folderName) {
			document.getElementById("errorMsgD").textContent = "";
			document.getElementById("okMsgD").textContent = "";
			container.style.visibility = "visible";
			document.getElementById("creaDocumento").style.visibility = "visible";
			form.getElementsByClassName("hiddenInput")[0].value = folderId;
			title.textContent = "Crea un documento nella cartella " + folderName;
			container.appendChild(form);
			let hbtn = document.getElementById("hideD");
			hbtn.addEventListener("click", function () {
				self.hide();
			});
		}
	}

	//Funzione che gestisce l'eliminazione di un documento: chiama il server per eseguire l'eliminazione
	function removeFile(idDocumento) {
		let xhttp = new XMLHttpRequest();

		let postObj = {
			idDoc: idDocumento
		}

		xhttp.onload = function () {
			if (xhttp.readyState == XMLHttpRequest.DONE) {
				if (xhttp.status == 200) {
					document.getElementById("hideContenutoCartellaBtn").click();
					document.getElementById("hideDettagliDocBtn").click();
					//document.getElementById("hideD").click();
					//document.getElementById("hideSC").click();
					pageOrchestrator.refresh();
				}
				else
					alert(xhttp.responseText);
			} else {
				if (xhttp.responseText.length == 0) {
					alert("An unexpected error occurred.");
				} else {
					alert(xhttp.responseText);
				}
			}
		}

		xhttp.open("POST", "EliminaDoc");
		xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
		xhttp.send(JSON.stringify(postObj));
		pageOrchestrator.refresh();
	}

	//Funzione che gestisce l'eliminazione di una cartella: chiama il server per eseguire l'eliminazione
	function removeFolder(idCartella) {
		let xhttp = new XMLHttpRequest();

		let postObj = {
			idCartella: idCartella
		}

		xhttp.onload = function () {
			if (xhttp.readyState == XMLHttpRequest.DONE) {
				if (xhttp.status == 200) {
					pageOrchestrator.refresh();
					document.getElementById("hideContenutoCartellaBtn").click();
					document.getElementById("hideDettagliDocBtn").click();
					document.getElementById("hideD").click();
					document.getElementById("hideSC").click();
				}
				else
					alert(xhttp.responseText);
			} else {
				if (xhttp.responseText.length == 0) {
					alert("An unexpected error occurred.");
				} else {
					alert(xhttp.responseText);
				}
			}
		}

		xhttp.open("POST", "EliminaCartella");
		xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
		xhttp.send(JSON.stringify(postObj));
	}

	//Funzione che gestisce lo spostamento di un documento: chiama il server per eseguire lo spostamento
	function move(idCartella, idDocumento) {
		let xhttp = new XMLHttpRequest();

		let postObj = {
			idPadre: idCartella,
			idDocumento: idDocumento
		}

		xhttp.onload = function () {
			if (xhttp.readyState == XMLHttpRequest.DONE) {
				if (xhttp.status == 200) {
					document.getElementById("hideContenutoCartellaBtn").click();
					document.getElementById("hideDettagliDocBtn").click();
					pageOrchestrator.refresh();
				}
				else
					alert(xhttp.responseText);
			} else {
				if (xhttp.responseText.length == 0) {
					alert("An unexpected error occurred.");
				} else {
					alert(xhttp.responseText);
				}
			}
		}

		xhttp.open("POST", "SpostaDoc");
		xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
		xhttp.send(JSON.stringify(postObj));
	}

	//Funzione per il drag and drop
	function allowDrop(ev) {
		ev.preventDefault();
	}

	//Funzione per il drag and drop
	function drag(ev) {
		ev.dataTransfer.setData("text", ev.target.id);
		//var data = ev.dataTransfer.getData("text");
	}

	//Funzione per il drag and drop: gestione dell'evento di drop sul cestino
	function dropBin(ev) {
		ev.preventDefault();
		var data = ev.dataTransfer.getData("text");

		if (confirm("Sicuro di voler eliminare questo elemento?")) {
			if (data.startsWith("documento_")) {
				let idDocumento = data.replace("documento_", "");
				removeFile(idDocumento);
			}
			else if (data.startsWith("cartella_")) {
				let idCartella = data.replace("cartella_", "");
				removeFolder(idCartella);
			}
		}
		return true;
	}

	//Funzione per il drag and drop: gestione dell'evento di drop su una cartella
	function dropFolder(ev) {
		ev.preventDefault();
		let data = ev.dataTransfer.getData("text");

		if (data.startsWith("cartella_")) {
			alert("Non puoi spostare le cartelle, puoi solo eliminarle");
			return;
		}

		let idDocumento = data.replace("documento_", "");
		let data2 = ev.target.id;
		let idCartella = data2.replace("cartella_", "");
		move(idCartella, idDocumento);
	}

	function PageOrchestrator() {

		this.start = () => {

			treeDiv = new Tree(document.getElementById("treeContainer"));
			treeDiv.show();
			//formContainer = new Form(document.getElementById("newCategoryForm"));

			document.getElementById("logout_btn").onclick = () => {
				let loggedOut = false;
				makeCall("GET", "Logout", function (response) {
					if (response.readyState === XMLHttpRequest.DONE) {
						switch (response.status) {
							case 200:
								loggedOut = true;
								localStorage.clear();
								sessionStorage.clear();
								//document.getElementById("okMsg").textContent = response.responseText;
								window.location.href = "login.html";
								break;
							default:
								alert("Error idk");
						}
					}
				});
				if (!loggedOut) {
					localStorage.clear();
					sessionStorage.clear();
					window.location.href = "login.html";
				}
			}

			contenutiCartella = new MostraContenuti(document.getElementById("contCentro"));

			creaSottocartella = new CreaSottocartella(document.getElementById("contenitoreDestra"), document.getElementById("creaSC"));
			creaDocumento = new CreaDocumento(document.getElementById("contenitoreDestra"), document.getElementById("creaD"));
			creaCartella = new CreaCartella(document.getElementById("contenitoreDestra"), document.getElementById("creaC"));

			document.getElementById("newFolderBtn").addEventListener("click", function () {
				creaCartella.enableForm();
			});

			this.hideContent();
		}

		this.refresh = () => {
			treeDiv.reset();
		}

		//Funzione che nasconde tutto eccetto la lista delle cartelle e sottocartelle
		this.hideContent = function () {
			contenutiCartella.hide();
			//contenutiCartella.hideDoc();
			//creaSottocartella.hide();
			//creaCartella.hide();
		}
	}
}