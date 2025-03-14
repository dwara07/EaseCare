// Get the search input and service list
const searchInput = document.getElementById("searchInput");
const serviceList = document.getElementById("ser-list");
let caretakerName1 = "";

// Event listener for the search input
searchInput.addEventListener("input", function() {
	const filter = searchInput.value.toLowerCase();
	const serviceCards = serviceList.getElementsByClassName("service-card");

	// Loop through each service card and hide those that don't match the search
	Array.from(serviceCards).forEach(function(card) {
		const title = card.querySelector(".title").textContent.toLowerCase();
		const category = card.querySelector(".category").textContent.toLowerCase();
		const location = card.querySelector(".location").textContent.toLowerCase();

		if (title.includes(filter) || category.includes(filter) || location.includes(filter)) {
			card.style.display = "";
		} else {
			card.style.display = "none";
		}
	});
});

document.querySelector(".service-particular-request-btn").addEventListener("click", function() {
	document.querySelector(".service-particular-fullbody").style.display = "none";
	document.getElementById("caretaker-all-details-container").style.display = "none";

	document.querySelector(".video-call-body").style.top = 0;
	document.querySelector(".video-call-body").style.left = 0;
	document.querySelector(".video-call-body").style.right = 0;

	let particularName = document.getElementById("service-particular-person-name").innerText;
	document.getElementById("video-call-peer").innerText = particularName;


	/*    videoCallPeerUsername = requestedName;
	*//*    videoCallInitialize();
		document.getElementById("caretaker-all-details-container").style.display = "none";
		document.querySelector(".video-call-body").style.display = "block";*/
	console.log("presseddd")
});


let role = "";
let origin = window.location.origin;
let loggedInUserName = "";
let totalExperience = "";
let emailAddress = "";
let caretakerDescription = "";

let country = "";
let address = "";
let city = "";
let state = "";
let pinCode = "";

let selectedCareTaker = "";
let selectedFile = null; // Store file temporarily


let notiId = 0;

document.addEventListener("DOMContentLoaded", function() {
	if (role !== "caretaker") {
		fetch(`${origin}/projectWithFullStack/fetchServicePercentage`)
			.then(response => response.json())
			.then(data => {
				document.querySelectorAll(".service-item").forEach(item => {
					let serviceName = item.querySelector(".service-name").textContent.trim();
					let percentage = data[serviceName] || 0; // Get percentage or default to 0
					item.querySelector(".service-percentage").textContent = percentage + "%";
				});
			})
			.catch(error => console.error("Error fetching data:", error));

		fetch(`${origin}/projectWithFullStack/fetchProgressStats`)
			.then(response => response.json())
			.then(data => {
				console.log("rate", data);
				document.querySelector(".progress-fill.success").style.width = data.successRate + "%";
				document.querySelector(".progress-fill.failure").style.width = data.failureRate + "%";

				// Calculate Client Happiness
				let happiness = Math.round((data.successRate - data.failureRate + 100) / 2);

				// Update HTML
				document.getElementById("client-happy-percentage").textContent = happiness + "%";
				document.getElementById("user-total-experience").innerText = "Enjoy Our App !";
			})
			.catch(error => console.error("Error fetching progress data:", error));


		fetch(`${origin}/projectWithFullStack/countRequests`)
			.then(response => response.json())
			.then(data => {
				console.log("Total Requests:", data);
				document.getElementById("hiring-count").innerText = data.requestCount;
			})
			.catch(error => console.error("Error fetching total requests:", error));

	}
	else {
		fetch(`${origin}/projectWithFullStack/countHiringNotifications`)
			.then(response => response.json())
			.then(data => {
				document.getElementById("hiring-count").innerText = data.hiringCount;
			})
			.catch(error => console.error("Error fetching hiring notifications:", error));

	}
});



document.getElementById("req-submit-btn").addEventListener("click", function() {
	let title = document.getElementById("service-title").value;
	let id = 0;
	console.log("dance", title);
	addJobToList({
		"id": id,
		"requesterName": loggedInUserName,
		"title": title
	});
});

document.querySelector(".caretaker-feedback-button").addEventListener("click", function(event) {
    event.preventDefault(); // Prevent form from submitting the default way

    const form = document.querySelector(".caretaker-feedback-form");
    const formData = new FormData(form);
    form.style.display = "block";
    let feedbackOverlay = document.querySelector('.feedback-overlay');
    feedbackOverlay.style.display = 'block';

    fetch(`${origin}/projectWithFullStack/feedback?` + new URLSearchParams(formData), {
        method: "GET",
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === "ok") {    
            alert("This feedback was submitted successfully! 🎉");
            document.querySelector(".caretaker-feedback-body").style.display = "none";
            form.style.display = "none"; // Hide form
            feedbackOverlay.style.display = "none"; // Hide overlay
        } else {
            alert("Oops! Something went wrong. Please try again. 😥");
        }
    })
    .catch(error => console.error("Error:", error));
});

const openButtons = document.querySelectorAll(".open-button"); // Assuming "Open" buttons have this class
const modal = document.getElementById("work-confirmation-modal");
const yesButton = document.getElementById("work-confirmation-yes");
const noButton = document.getElementById("work-confirmation-no");

openButtons.forEach(button => {
	button.addEventListener("click", function() {
		let rId = this.getAttribute("data-rid"); // Get request ID from button
		checkRequestStatus(rId);
	});
});

// Close modal when "No" is clicked
noButton.addEventListener("click", function() {
	modal.style.display = "none";
});

// When "Yes" is clicked, send request to server
yesButton.addEventListener("click", function() {
	let rId = yesButton.getAttribute("data-rid");
	console.log("rid ", rId)
	updateWorkStatus(rId);
	modal.style.display = "none"; // Hide modal after confirmation
});

/*if (role === "caretaker") {
*/

function updateWorkStatus(rId) {
	fetch(`${origin}/projectWithFullStack/updateWorkStatus`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ rId: rId, nId: notiId })
	})
		.then(response => response.json())
		.then(data => {
			if (data.success) {
				showWokConfirmationPopup("✅ Work accepted successfully! The requester will contact you soon.");

				fetchJobDetails(rId);
				/*			fetchNotifications(); // Refresh notifications*/
			} else {
				showWokConfirmationPopup("❌ Error updating status! Try again.");
			}
		})
		.catch(error => console.error("Error:", error));
}


function fetchJobDetails(rId) {
	fetch(`${origin}/projectWithFullStack/getJobDetails?action=fetchJobDetails&jobId=${rId}`)
		.then(response => response.json())
		.then(data => {
			console.log("job", data);

			if (data.status === "success" && data.data.length > 0) {
				addJobToList(data.data[0]); // ✅ Pass the first job object
			} else {
				console.error("No valid job data received:", data);
			}
		})
		.catch(error => console.error("Error:", error));
}


function addJobToList(job) {
	console.log("jojbbbbbc", job);
	const jobsContainer = document.getElementById("jobs-container");
	const noJobsMessage = document.getElementById("no-jobs-message");

	if (noJobsMessage) noJobsMessage.style.display = "none";

	const jobItem = document.createElement("div");
	jobItem.classList.add("job-item");
	jobItem.id = job.id;

	if (job.title) {

		if (role === "caretaker") {
			jobItem.innerHTML = `
	        <p><strong>${job.requesterName || "Unknown"}</strong> - ${job.title || "No Title"}</p>	    `;

			if (job.title.includes("feedback") && job.title.includes("report")) {
				jobItem.innerHTML += `<button class='open-button'>OPEN</button>`;
			}

		} else {
			if (job.required) {
				jobItem.innerHTML = `
				<p><strong>${job.title || "Unknown"}</strong> - ${job.required || "No Required"}</p>
				<button class='open-button'>OPEN</button>	
			`;
			} else {
				if (job.title.includes("accepted")) {
					jobItem.innerHTML = `
					<p class='work-accepted'><strong>${job.title || "No Title"}</strong></p>
					<button class='feedback-button feedback-form-${job.id}'>Feedback Form</button>
					<button class='open-button'>OPEN</button>	
				`;

					// ✅ Add event listener for the open button
					const openButton = jobItem.querySelector(".open-button");
					if (openButton) {
						openButton.addEventListener("click", function() {
							showServiceDetails(job.rId, job.id);
						});
					}

				} else {
					jobItem.innerHTML = `
					<p><strong>${job.title || "No Title"}</strong></p>
					<button class='open-button'>OPEN</button>	
				`;
				}
			}
		}
	}
	// ✅ Add event listener for feedback button (Only if it exists)
	const feedbackButton = jobItem.querySelector(`.feedback-form-${job.id}`);
	if (feedbackButton) {
		feedbackButton.addEventListener("click", function() {
			let workAcceptedText = jobItem.querySelector(".work-accepted").innerText;
			let formPerson = workAcceptedText.split("by ")[1];

			console.log(`Feedback clicked: feedback-form-${job.id}`);
			console.log("Extracted Name:", formPerson);

			document.querySelector(".caretaker-feedback-body").style.display = "flex";
			let nameInput = document.getElementById("caretaker-feedback-name");
			nameInput.value = formPerson;
			nameInput.readOnly = true; // Make it read-only

			document.getElementById("caretaker-feedback-comments").value = "";
		});

	}

	/*	// ✅ Add event listener for the open button
		const openButton = jobItem.querySelector(".open-button");
		if (openButton) {
			openButton.addEventListener("click", function() {
				showServiceDetails(job.rId, job.id);
			});
		}
	*/
	jobsContainer.appendChild(jobItem);
}


function showServiceDetails(rId, id) {
	// you have notification id so using that extract the user name and ctrl F - GetParticularCareTakerDetails ....
	console.log("clibjhv", rId, id);
	fetch(`${origin}/projectWithFullStack/getNameByNotificationId?notification_id=${id}`)
		.then(response => response.json())
		.then(data => {
			if (data.user_name) {
				selectedCareTaker = data.user_name;
				fetchParticularCareTakerDetails();
				console.log("User Name:", data.user_name);
			} else {
				console.error("Error:", data.error);
			}
		})
		.catch(error => console.error("Fetch error:", error));


	/*DO UI TO FULL DETAILS*/
	/*let savedJobs = JSON.parse(localStorage.getItem("savedJobs")) || [];
	let job = savedJobs.find(j => j.rId == rId);

	if (job) {
		updateServicePage(job);
	} else {
		fetch(`${origin}/projectWithFullStack/getJobDetails?action=showServiceDetails&jobId=${rId}`)
			.then(response => response.json())
			.then(data => updateServicePage(data.data[0]))
			.catch(error => console.error("Error:", error));
	}*/
}

function updateServicePage(job) {
	console.log("full job details", job);
}


function loadJobsFromDatabase() {
	const jobsContainer = document.getElementById("jobs-container");
	jobsContainer.innerHtml = "";
	const noJobsMessage = document.getElementById("no-jobs-message");

	fetch(`${origin}/projectWithFullStack/fetchSavedJobs`)
		.then(response => response.json())
		.then(savedJobs => {
			console.log("saved", savedJobs);
			if (savedJobs.length > 0 && noJobsMessage) {
				noJobsMessage.style.display = "none";
			}
			savedJobs.forEach(job => addJobToList(job)); // ✅ Correct mapping
		})
		.catch(error => console.error("Error fetching jobs:", error));

}


function checkRequestStatus(rId) {
	fetch(`${origin}/projectWithFullStack/checkStatus?rId=${rId}`)
		.then(response => response.json())
		.then(data => {
			if (data.status === "open") {
				const yesButton = document.getElementById("work-confirmation-yes");
				yesButton.setAttribute("data-rid", rId);
				document.getElementById("work-confirmation-modal").style.display = "flex";
			} else {
				showWokConfirmationPopup("❌ Someone already hired for this request! Stay active for more notifications!");
			}
		})
		.catch(error => console.error("Error:", error));
}

function showWokConfirmationPopup(message) {
	console.log(message);
	const popup = document.createElement("div");
	popup.classList.add("wok-confirmation-popup");

	const popupMessage = document.createElement("p");
	popupMessage.textContent = message;

	const closeButton = document.createElement("button");
	closeButton.textContent = "OK";
	closeButton.classList.add("wok-confirmation-popup-close");
	closeButton.onclick = function() {
		popup.remove();
	};

	popup.appendChild(popupMessage);
	popup.appendChild(closeButton);
	document.body.appendChild(popup);
}

/*}//end of caretaker
*/


// Handle settings Submit Button Click
document.getElementById("settings-submit-btn").addEventListener("click", function() {
	let selectedTheme = "light";

	let formData = new URLSearchParams();
	formData.append("country", document.querySelector("input[name='country']").value);
	formData.append("state", document.querySelector("input[name='state']").value);
	formData.append("city", document.querySelector("input[name='city']").value);
	formData.append("street", document.querySelector("input[name='street']").value);
	formData.append("pin_code", document.querySelector("input[name='pin_code']").value);

	if (role === "caretaker") {

		formData.append("notify_all", document.querySelector("input[name='notify_all']").checked);
		formData.append("receive_mail_from_carelink", document.querySelector("input[name='receive_mail_from_carelink']").checked);
		formData.append("selectedTheme", selectedTheme); // Append theme
	}
	let isCaretaker = (role === "caretaker") ? true : false;

	fetch(`${origin}/projectWithFullStack/UpdateSettings?isCaretaker=${isCaretaker}`, {
		method: "POST",
		body: formData,
		headers: { "Content-Type": "application/x-www-form-urlencoded" }
	}).then(response => {
		if (response.ok) {
			alert("Settings updated successfully!");
		} else {
			alert("Failed to update settings.");
		}
	}).catch(error => console.error("Error:", error));
});



document.querySelector(".edit-profile-btn").addEventListener("click", fetchProfile);

function fetchProfile() {
	fetch(`${origin}/projectWithFullStack/getProfilePhoto`)
		.then(response => response.json())
		.then(data => {
			console.log("data image", data, "path  ", data.profilePhotoUrl);
			document.querySelector(".avatar").src = data.profilePhotoUrl;
			document.querySelector(".profile-photo").src = data.profilePhotoUrl;
			document.querySelector(".profile-avatar-large").src = data.profilePhotoUrl;
		});
}

document.getElementById("fileInput").addEventListener("change", uploadPhotoPreview);

function triggerFileUpload() {
	document.getElementById("fileInput").click();
}

// Store file and update preview (but don't upload yet)
function uploadPhotoPreview() {
	let fileInput = document.getElementById("fileInput");
	selectedFile = fileInput.files[0]; // Store file for later use

	if (!selectedFile) {
		alert("Please select an image first!");
		return;
	}

	// Show the preview
	let imageUrl = URL.createObjectURL(selectedFile);
	document.querySelector(".profile-photo").src = imageUrl;
}

// Upload file when "Save" is clicked
function savePhoto() {
	if (!selectedFile) {
		alert("No new image selected!");
		return;
	}
	let formData = new FormData();
	formData.append("profilePhoto", selectedFile);

	fetch(`${origin}/projectWithFullStack/profile?origin=${origin}`, {
		method: "POST",
		body: formData
	})
		.then(response => response.json())
		.then(data => {
			let imageUrl = `${origin}${data.path}`;
			console.log("Final Image URL:", imageUrl);

			// Update the profile images with the final stored URL
			document.querySelector(".avatar").src = imageUrl;
			document.querySelector(".profile-photo").src = imageUrl;
			document.querySelector(".profile-avatar-large").src = imageUrl;

			alert("Profile photo updated successfully!"); // Success message
		})
		.catch(error => alert("Upload failed! " + error));

	// Reset selected file after saving
	selectedFile = null;
}



function createCaretakerDetails(particularCaretaker) {
	let caretaker = particularCaretaker[0];
	// caretaker = particulaCaretakerJson;
	console.log("aaaaa", caretaker);
	console.log("type", typeof (caretaker));

	console.log("cname", caretaker.caretakerName);

	// console.log("uuuu",particulaCaretakerJson);
	console.log("custom  ", caretaker.customizedAddress);

	let mainContainer = document.getElementById("caretaker-all-details-container");
	mainContainer.style.display = "flex";

	mainContainer.innerHTML = "";

	let leftWholeDiv = document.createElement("div");
	leftWholeDiv.classList.add("caretaker-all-details-whole-sidebar");

	let nameProfileDiv = document.createElement("div");
	nameProfileDiv.classList.add("caretaker-all-details-sidebar");

	let imgTag = document.createElement("img");
	imgTag.classList.add("caretaker-profile-picture");
	imgTag.src = caretaker.profile;

	nameProfileDiv.appendChild(imgTag);

	leftWholeDiv.appendChild(nameProfileDiv);

	let wholeServiceInterestDiv = document.createElement("div");

	let serviceh3 = document.createElement("h3");
	serviceh3.innerText = "Services Interested In";
	serviceh3.classList.add("serviceh3");

	let serviceInterestDiv = document.createElement("div");
	serviceInterestDiv.classList.add("caretaker-all-details-services");

	// Assuming `caretaker.servicesInterested` contains the fetched comma-separated services
	let services = caretaker.servicesInterested.split(",").map(service => service.trim());

	services.forEach(service => {
	    let spanInterest = document.createElement("span");
	    spanInterest.innerText = service;
	    serviceInterestDiv.appendChild(spanInterest);
	});

	wholeServiceInterestDiv.appendChild(serviceh3);
	wholeServiceInterestDiv.appendChild(serviceInterestDiv);


	leftWholeDiv.appendChild(wholeServiceInterestDiv);

	let rightWholeSidebar = document.createElement("div");
	rightWholeSidebar.classList.add("caretaker-all-details-right-sidebar");


	let h2Name = document.createElement("div");
	h2Name.innerHTML = '<i class="fa-solid fa-xmark"></i>';
	h2Name.classList.add("caretaker-profile-cross");

	rightWholeSidebar.appendChild(h2Name);



	let rightCareTakerBiodata = document.createElement("div");
	rightCareTakerBiodata.classList.add("caretaker-full-biodata");



	let caretakerHeader = document.createElement("div");
	caretakerHeader.classList.add("caretaker-all-details-header");

	let caretakerName = document.createElement("h1");
	caretakerName.innerText = caretaker.caretakerName;
	caretakerName1 = caretaker.caretakerName;
	console.log("1", caretakerName1)

	let addressSpan = document.createElement("span");
	addressSpan.innerText = caretaker.customizedAddress;
	addressSpan.classList.add("caretaker-all-details-location");
	console.log(caretaker.address);

	caretakerHeader.appendChild(caretakerName);
	caretakerHeader.appendChild(addressSpan);

	rightCareTakerBiodata.appendChild(caretakerHeader);

	let caretakerRatingDetails = document.createElement("div");
	caretakerRatingDetails.classList.add("caretaker-all-details-rating");




	/*	let starRatingWholeDiv = document.createElement("div");
		starRatingWholeDiv.classList.add("caretaker-all-details-whole-star")
	
		let spanRatingScore = document.createElement("span");
		spanRatingScore.classList.add("caretaker-all-details-rating-score");
		spanRatingScore.innerText = 4;
	
		let starsDiv = document.createElement("div");
		starsDiv.classList.add("caretaker-all-details-stars");
	
		let spanStar1 = document.createElement("a");
		let spanStar2 = document.createElement("a");
		let spanStar3 = document.createElement("a");
		let spanStar4 = document.createElement("a");
		let spanStar5 = document.createElement("a");
	
		spanStar1.innerHTML = '<i class="fa-solid fa-star rating-star" style="color: #FFD43B;"></i>';
		spanStar2.innerHTML = '<i class="fa-solid fa-star rating-star" style="color: #FFD43B;"></i>';
		spanStar3.innerHTML = '<i class="fa-solid fa-star rating-star" style="color: #FFD43B;"></i>';
		spanStar4.innerHTML = '<i class="fa-solid fa-star rating-star" style="color: #FFD43B;"></i>';
		spanStar5.innerHTML = '<i class="fa-regular fa-star rating-star" style="color: #FFD43B;"></i>';
	
		starsDiv.append(spanStar1, spanStar2, spanStar3, spanStar4, spanStar5);
		   starsDiv.appendChild(spanStar1);
		   starsDiv.appendChild(spanStar2);
		   starsDiv.appendChild(spanStar3);
		   starsDiv.appendChild(spanStar4);
		   starsDiv.appendChild(spanStar5);
	
		starRatingWholeDiv.appendChild(spanRatingScore);
		starRatingWholeDiv.appendChild(starsDiv);
	
		caretakerRatingDetails.appendChild(starRatingWholeDiv);*/

	let caretakerActions = document.createElement("div");
	caretakerActions.classList.add("caretaker-all-details-btns")

	let messageButton = document.createElement("button");
	messageButton.classList.add("caretaker-all-details-message-btn");
	messageButton.innerText = "Request Meeting";

	messageButton.addEventListener("click", function() {
		videoCallInitialize();
		document.getElementById("caretaker-all-details-container").style.display = "none";
		document.querySelector(".video-call-body").style.top = 0; document.querySelector(".video-call-body").style.left = 0; document.querySelector(".video-call-body").style.right = 0;
	})

	let hireButton = document.createElement("button");
	hireButton.classList.add("caretaker-all-details-hire-btn");

	hireButton.addEventListener("click", () => {
		// Create the popup background
		let popupBackground = document.createElement("div");
		popupBackground.classList.add("hiring-particular-caretaker-popup-background");

		// Create the popup container
		let popupContainer = document.createElement("div");
		popupContainer.classList.add("hiring-particular-caretaker-popup-container");

		// Heading
		let popupHeading = document.createElement("h2");
		popupHeading.innerText = "Enter Hiring Details";

		// Amount Input
		let amountLabel = document.createElement("label");
		amountLabel.innerText = "Amount:";
		let amountInput = document.createElement("input");
		amountInput.type = "number";
		amountInput.placeholder = "Enter amount";
		amountInput.required = true;

		// Currency Select
		let currencyLabel = document.createElement("label");
		currencyLabel.innerText = "Currency:";
		let currencySelect = document.createElement("select");

		let currencies = [
			{ symbol: "₹", name: "INR - Indian Rupee" },
			{ symbol: "$", name: "USD - US Dollar" },
			{ symbol: "€", name: "EUR - Euro" },
			{ symbol: "£", name: "GBP - British Pound" },
			{ symbol: "¥", name: "JPY - Japanese Yen" },
			{ symbol: "$", name: "AUD - Australian Dollar" },
			{ symbol: "$", name: "SGD - Singapore Dollar" },
		];

		currencies.forEach(curr => {
			let option = document.createElement("option");
			option.value = curr.symbol;
			option.innerText = curr.name;
			currencySelect.appendChild(option);
		});

		// Payment Duration Select
		let durationLabel = document.createElement("label");
		durationLabel.innerText = "Payment Duration:";
		let durationSelect = document.createElement("select");

		let option1 = document.createElement("option");
		option1.value = "per month";
		option1.innerText = "Per Month";

		let option2 = document.createElement("option");
		option2.value = "per day";
		option2.innerText = "Per Day";

		durationSelect.appendChild(option1);
		durationSelect.appendChild(option2);

		// Buttons
		let confirmButton = document.createElement("button");
		confirmButton.innerText = "Confirm";
		confirmButton.classList.add("hiring-particular-caretaker-confirm-btn");

		let cancelButton = document.createElement("button");
		cancelButton.innerText = "Cancel";
		cancelButton.classList.add("hiring-particular-caretaker-cancel-btn");

		cancelButton.addEventListener("click", () => {
			document.body.removeChild(popupBackground);
		});

		confirmButton.addEventListener("click", () => {
			let amount = amountInput.value;
			let currency = currencySelect.value;
			let duration = durationSelect.value;

			if (!amount) {
				alert("Please enter an amount!");
				return;
			}

			fetch(`${origin}/projectWithFullStack/HireCaretakerServlet`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({
					caretakerName: caretakerName.innerText,
					amount: amount,
					currency: currency,
					duration: duration
				})
			})
				.then(response => response.json())
				.then(result => {
					if (result.success) {
						alert("Hired Successfully! 🎉");
					} else {
						alert("Something went wrong! 😢");
					}
					document.body.removeChild(popupBackground);
				})
				.catch(error => {
					alert("Error occurred! ❌");
					console.error(error);
				});
		});

		// Append Elements
		popupContainer.appendChild(popupHeading);
		popupContainer.appendChild(amountLabel);
		popupContainer.appendChild(amountInput);
		popupContainer.appendChild(currencyLabel);
		popupContainer.appendChild(currencySelect);
		popupContainer.appendChild(durationLabel);
		popupContainer.appendChild(durationSelect);
		popupContainer.appendChild(confirmButton);
		popupContainer.appendChild(cancelButton);
		popupBackground.appendChild(popupContainer);
		document.body.appendChild(popupBackground);
	});




	hireButton.innerText = "Hire";

	let reportButton = document.createElement("button");
	reportButton.classList.add("caretaker-all-details-report-btn");
	reportButton.innerText = "Report";


	const popup = document.getElementById("particular-caretaker-popup");
	const overlay = document.getElementById("particular-caretaker-overlay");
	const closeButton = document.getElementById("particular-caretaker-close");
	const complaintForm = document.getElementById("particular-caretaker-form");

	let reportingPerson = document.getElementById("particular-caretaker-from");
	let reportingToPerson = document.getElementById("particular-caretaker-to");

	reportingPerson.value = document.getElementById("profile-name").innerText;
	reportingToPerson.value = caretakerName.innerText;

	// Open popup
	reportButton.addEventListener("click", function() {
		popup.style.display = "block";
		overlay.style.display = "block";
	});

	// Close popup
	closeButton.addEventListener("click", function() {
		popup.style.display = "none";
		overlay.style.display = "none";
	});

	// Hide popup when clicking outside
	overlay.addEventListener("click", function() {
		popup.style.display = "none";
		overlay.style.display = "none";
	});

	// Ensure complaint is not empty
	complaintForm.addEventListener("submit", function(event) {
		event.preventDefault(); // Stop form submission

		const complaintText = document.getElementById("particular-caretaker-complaint").value.trim();

		if (!complaintText) {
			alert("Please enter your complaint before submitting!");
		} else {
			// Send data to ReportUserServlet
			fetch(`${origin}/projectWithFullStack/ReportUserServlet`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({
					reportingPerson: document.getElementById("particular-caretaker-from").value,
					reportedPerson: document.getElementById("particular-caretaker-to").value,
					complaint: complaintText
				})
			})
				.then(response => response.json())
				.then(data => {
					if (data.success) {
						alert("✅ Report submitted successfully!");
						// Hide popup after submission
						popup.style.display = "none";
						overlay.style.display = "none";
					} else {
						alert("❌ Failed to submit report. Please try again.");
					}
				})
				.catch(error => {
					console.error("Error:", error);
					alert("❌ An error occurred.");
				});
		}
	});


	caretakerActions.appendChild(messageButton);
	caretakerActions.appendChild(hireButton);
	caretakerActions.appendChild(reportButton);

	caretakerRatingDetails.appendChild(caretakerActions);
	rightCareTakerBiodata.appendChild(caretakerRatingDetails);

	let rightCareTakerPersonalInfo = document.createElement("div");
	rightCareTakerPersonalInfo.classList.add("caretaker-personal-info");

	/*   let caretakerWholeInfo = document.createElement("div");
	   caretakerWholeInfo.classList.add("caretaker-personal-info-whole-one")
	   */
	let nameAgeDiv = document.createElement("div");
	nameAgeDiv.classList.add("caretaker-personal-info-div");

	let personalInfoH3 = document.createElement("h3");
	personalInfoH3.innerText = "Personal Information";
	let caretakerFullName = document.createElement("p");
	caretakerFullName.innerHTML = `<strong>Full name:</strong> ${caretaker.caretakerName}`;
	let caretakerAge = document.createElement("p");
	caretakerAge.innerHTML = `<strong>Age:</strong> ${caretaker.age}`;
	let caretakerFullAddress = document.createElement("p");
	caretakerFullAddress.innerHTML = `<strong>Full Address:</strong> ${caretaker.address}`;


	nameAgeDiv.appendChild(personalInfoH3);
	nameAgeDiv.appendChild(caretakerFullName);
	nameAgeDiv.appendChild(caretakerAge);
	nameAgeDiv.appendChild(caretakerFullAddress);

	rightCareTakerPersonalInfo.appendChild(nameAgeDiv);

	let expdiv = document.createElement("div");
	expdiv.classList.add("caretaker-personal-exp-div");

	let expInfoH3 = document.createElement("h3");
	expInfoH3.innerText = "Experience";
	let experience = document.createElement("p");
	experience.innerHTML = `<strong>${caretaker.exp}</strong> `;


	expdiv.append(expInfoH3, experience);
	rightCareTakerPersonalInfo.appendChild(expdiv)

	let caretakerWholeBioDescription = document.createElement("div");
	caretakerWholeBioDescription.classList.add("caretaker-personal-bio-div");

	let bioText = document.createElement("h3");
	bioText.innerText = "Bio";


	let description = document.createElement("p");
	description.innerText = caretaker.bio;


	caretakerWholeBioDescription.appendChild(bioText);
	caretakerWholeBioDescription.appendChild(description);

	rightCareTakerPersonalInfo.appendChild(caretakerWholeBioDescription);


	rightWholeSidebar.appendChild(rightCareTakerBiodata);
	rightWholeSidebar.appendChild(rightCareTakerPersonalInfo);

	mainContainer.appendChild(leftWholeDiv);
	mainContainer.appendChild(rightWholeSidebar);



	document.querySelector(".caretaker-profile-cross").addEventListener("click", () => {
		document.querySelectorAll(".nav-item").forEach(ele => ele.disabled = false);
		document.querySelector(".all-caretaker-container").style.display = "block";
		document.querySelector(".caretaker-all-details-container").style.display = "none";

	})

}


function fetchParticularCareTakerDetails() {
	console.log("par    ", selectedCareTaker)
	fetch(`${origin}/projectWithFullStack/GetParticularCareTakerDetails?caretakerName=${selectedCareTaker}`)
		.then(response => response.json())
		.then(data => {
			console.log("partic", data);
			// particulaCaretakerJson = data;
			createCaretakerDetails(data);
			console.log("finished....");

		});
}


function createCaretakerCard(caretaker) {
	// Create main card container
	const card = document.createElement("div");
	card.classList.add("care-profile-card");

	// Add event listener to log class name when clicked
	card.addEventListener("click", function() {
		selectedCareTaker = card.className.split(" ")[1];
		console.log("Clicked card class name:", selectedCareTaker);
		document.querySelector(".all-caretaker-container").style.display = "none";
		document.querySelectorAll(".nav-item").forEach(ele => ele.disabled = true);

		fetchParticularCareTakerDetails();
	});
console.log("creeqqq" , caretaker);
	// Create avatar div
	const avatar = document.createElement("img");
	avatar.classList.add("care-avatar");
	avatar.src = caretaker.profile;

	// Create profile info container
	const profileInfo = document.createElement("div");
	profileInfo.classList.add("care-profile-info");

	// Caretaker name
	const name = document.createElement("h2");
	name.classList.add("care-name");
	name.textContent = caretaker.caretakerName;

	card.classList.add(caretaker.caretakerName);

	const description = document.createElement("p");
	description.classList.add("care-desc");
	description.textContent = caretaker.description;

	// Append name and location to profile header
	profileInfo.appendChild(name);
	profileInfo.appendChild(description);

	const locAndExp = document.createElement("div");
	locAndExp.classList.add("care-expAndLoc");

	// Location
	const location = document.createElement("span");
	location.classList.add("care-location");
	location.textContent = `${caretaker.state}, ${caretaker.country}`;

	// Experience
	const experience = document.createElement("div");
	experience.classList.add("care-exp");
	experience.textContent = `${caretaker.experience} Exp`;

	locAndExp.appendChild(location);
	locAndExp.appendChild(experience);

	// Append everything to the card
	card.appendChild(avatar);
	card.appendChild(profileInfo);
	card.appendChild(locAndExp);


	return card;
}


// Function to render caretaker cards
function renderAllCaretakers(caretakers) {
	const container = document.querySelector(".all-caretaker-container");
	container.innerHTML = ""; // Clear previous cards

	caretakers.forEach(caretaker => {
		const card = createCaretakerCard(caretaker);
		container.appendChild(card);
	});
}

// Fetch data and display caretakers
function getAllCareTakers() {
	fetch(`${origin}/projectWithFullStack/getAllCareTakers`, { method: "POST" })
		.then(response => response.json())
		.then(data => {
			console.log("Received caretakers:", data);
			renderAllCaretakers(data);
		})
		.catch(error => console.error("Error fetching caretakers:", error));

}
function checkRole() {
	console.log("rrr", role)
	if (role === "caretaker") {
		fetch(`${origin}/projectWithFullStack/caretakerProgress`)
			.then(response => response.json())
			.then(data => {
				console.log("Caretaker Progress:", data);

				let successRate = data.successCount;
				let failureRate = data.failureCount;
				let happinessPercentage = data.happinessPercentage;

				let total = successRate + failureRate;
				let successWidth = total === 0 ? 0 : (successRate / total) * 100;
				let failureWidth = total === 0 ? 0 : (failureRate / total) * 100;

				document.querySelector(".progress-fill.success").style.width = successWidth + "%";
				document.querySelector(".progress-fill.failure").style.width = failureWidth + "%";
				document.getElementById("client-happy-percentage").innerText = happinessPercentage + "%";
			})
			.catch(error => console.error("Error fetching caretaker progress:", error));


		fetch(`${window.location.origin}/projectWithFullStack/serviceComposition`)
			.then(response => response.json())
			.then(data => {
				console.log("Service Composition Data:", data);

				document.querySelectorAll(".service-item").forEach(item => {
					let serviceName = item.querySelector(".service-name").textContent.trim();
					let foundService = data.services.find(service => service.name === serviceName);

					if (foundService) {
						item.querySelector(".service-percentage").innerText = foundService.percentage + "%";
					} else {
						item.querySelector(".service-percentage").innerText = "0%";
					}
				});
			})
			.catch(error => console.error("Error fetching service composition:", error));

		setInterval(fetchNotificationCount, 8000);
	}
	if (role === "need-care") {
		setInterval(fetchNotificationCount, 8000);
		console.log("indsssss");
		document.getElementById("none-notify1").style.display = "none";
		document.getElementById("none-notify2").style.display = "none";
	}
}

function fetchAddressDetails() {
	fetch(`${origin}/projectWithFullStack/getAddressAndSettingsInfo`, { method: "POST" })
		.then(response => response.json())
		.then(data => {
			console.log("Address Data:", data);

			// Assign values to form fields (without optional chaining)
			let countryInput = document.querySelector("#settings-country input");
			let addressInput = document.querySelector("#settings-address input");
			let cityInput = document.querySelector("#settings-city input");
			let stateInput = document.querySelector("#settings-state input");
			let zipInput = document.querySelector("#settings-zip-code input");

			if (countryInput) countryInput.value = data.country || "";
			if (addressInput) addressInput.value = data.street || "";
			if (cityInput) cityInput.value = data.city || "";
			if (stateInput) stateInput.value = data.state || "";
			if (zipInput) zipInput.value = data.pincode || "";

			// ✅ Set notification settings safely
			let notifyAllBtn = document.querySelector("#notify-all-toggle");
			let notifyOnlyBtn = document.querySelector("#notify-only-toggle");
			let receiveMailBtn = document.querySelector("#receive-mail-toggle");

			if (notifyAllBtn) notifyAllBtn.checked = data.notify_all === "true";
			if (notifyOnlyBtn) notifyOnlyBtn.checked = data.notify_all !== "true";
			if (receiveMailBtn) receiveMailBtn.checked = data.receive_mail_from_carelink === "true";

			// ✅ Apply theme settings
			let themeButtons = document.querySelectorAll(".toggle-buttons button");
			if (themeButtons.length > 0) {
				themeButtons.forEach(btn => btn.classList.remove("active")); // Remove all active classes
				let selectedTheme = data.theme === "dark" ? "dark" : "light"; // Default to light
				let activeThemeBtn = document.querySelector(`.toggle-buttons button[data-theme='${selectedTheme}']`);
				if (activeThemeBtn) activeThemeBtn.classList.add("active");
			}
		})
		.catch(error => console.error("Error fetching address details:", error));
}



function fetchDetailsForDashboard() {
	// Fetch the logged-in user's name
	fetch(`${origin}/projectWithFullStack/getLoggedInUser`)
		.then(response => response.json())
		.then(data => {
			console.log("sss", role)
			if (role === "need-care") {
				document.querySelector(".stat-card h3").innerText = "Total Request";
				document.getElementById("dashboard-exp").style.display = "none";
			}

			console.log("dasj")
			// After fetching the name, store it
			loggedInUserName = data.name;
			/*            document.getElementById("video-call-username").innerText = loggedInUserName;
						console.log("userrrrr    ", document.getElementById("video-call-username").innerText)*/
			console.log("Logged user:", loggedInUserName);
			document.getElementById("logged-user").innerText = `Hello, ${loggedInUserName}!`;

			// After getting the logged-in user name, fetch the user's experience and Gmail
			return fetch(`${origin}/projectWithFullStack/getCareTakerBioDetails`);
		})
		.then(response => response.json())
		.then(data => {
			// After fetching the experience and Gmail, process and display them
			totalExperience = (role === "caretaker") ? data.experience : 0;
			emailAddress = data.email;
			console.log("Experience:", totalExperience, data);
			console.log("Gmail:", emailAddress);

			// Update the dashboard with the experience and email
			if (role === "caretaker") {
				document.getElementById("total-title").innerText = 'Total Hired Job';
				document.getElementById("user-total-experience").innerText = `${totalExperience} Exp`;
			}
			document.getElementById("profile-name").innerText = `${loggedInUserName}`;
			document.getElementById("set-email").querySelector("input").value = emailAddress;
			document.getElementById("full-name").value = loggedInUserName;
			document.getElementById("edit_mobile").value = data.phone;
			if (role === "caretaker") {
				fetch(`${origin}/projectWithFullStack/countHiringNotifications`)
					.then(response => response.json())
					.then(data => {
						document.getElementById("hiring-count").innerText = data.hiringCount;
					})
					.catch(error => console.error("Error fetching hiring notifications:", error));
				document.getElementById("edit-profile-bio-textarea").value = data.bio;
			}
			if (role === "need-care") {
				document.querySelector(".bio-section").style.dispaly = "none";
			}

			// 1️⃣ Set Gender Radio Button
			if (data.gender) {
				let genderRadio = document.querySelector(`input[name="gender"][value="${data.gender.toLowerCase()}"]`);
				if (genderRadio) {
					genderRadio.checked = true;
				}
			}

			if (data.services) {
				let selectedServices = data.services.split(",").map(service =>
					service.trim().toLowerCase().replace(" ", "-") // ✅ Replace spaces with hyphens
				);

				selectedServices.forEach(service => {
					console.log("Checking service:", service);
					let checkbox = document.getElementById(service);
					console.log("cb", checkbox);

					if (checkbox) {
						checkbox.checked = true;
					}
				});
			}
			// If the experience is "Fresher", set it to 0
			totalExperience = (totalExperience === "Fresher") ? 0 : totalExperience;
		})
		.catch(error => {
			// Handle errors in case of failed fetch requests
			console.error('Error fetching data:', error);
		});
}






function loadUserInfo() {
	let totalServiceToDo = 0;
	let dateAndTotalService = document.getElementById("day-and-total-service");
	const today = new Date();
	const options = { weekday: 'long', month: 'long', day: 'numeric' };
	const formattedDate = today.toLocaleDateString('en-US', options);
	console.log(formattedDate); // Example: "Monday, January 28"
	dateAndTotalService.innerText = `Today is ${formattedDate}!`;
}

function fetchNotificationCount() {
	fetch(`${origin}/projectWithFullStack/fetchNotificationCount`, { method: "POST" })
		.then(response => response.json())
		.then(data => {
			console.log("fetch count adat ", data, "is request  ", data.isRequest);
			if (role === "caretaker") {
				updateBellIconCount(data.notificationCount);

				if (data.message !== "empty" && data.message !== undefined) {
					if (data.hasHiredNotification || data.hasReportNotification || !data.isRequest || data.hasFeedbackNotification) {
						addJobToList({
							"id": data.id,
							"requesterName": data.name,
							"title": data.message
						});
					}
				}
			}
			else {
				updateBellIconCount(data.count);
/*				loadJobsFromDatabase();
*/			}
		})
		.catch(error => console.error('Error fetching notification count:', error));
}



function updateBellIconCount(count) {
	const bellIcon = document.getElementById('bellIcon');
	let countBadge = bellIcon.querySelector('.count-badge');

	if (!countBadge) {
		countBadge = document.createElement('span');
		countBadge.classList.add('count-badge');
		bellIcon.appendChild(countBadge);
	}
	countBadge.textContent = count > 0 ? count : '';
}

document.getElementById('bellIcon').addEventListener('click', function(event) {
	const notificationsList = document.getElementById('notificationsList');

	if (notificationsList.classList.contains("open")) {
		const countBadge = document.querySelector('#bellIcon .count-badge');
		countBadge.innerText = "";
		notificationsList.classList.remove("open");
	} else {
		// First, mark unread notifications as "read" in the database
		markNotificationsAsRead().then(() => {
			fetchNotifications();
			fetchNotificationCount(); // Refresh count after marking as read
		});
		notificationsList.classList.add("open");
	}
	event.stopPropagation();
});


// Function to send a request to the servlet to mark notifications as read
function markNotificationsAsRead() {
	return fetch(`${origin}/projectWithFullStack/MarkNotificationsAsReadServlet`, { method: "POST" })
		.then(response => response.json())
		.catch(error => console.error('Error marking notifications as read:', error));
}


function fetchNotifications() {
	fetch(`${origin}/projectWithFullStack/fetchNotifications`, { method: "POST" })
		.then(response => response.json())
		.then(data => {
			const notificationItems = document.getElementById('notificationItems');
			notificationItems.innerHTML = "";

			if (data.notifications.length > 0) {
				data.notifications.forEach(notification => {
					let listItem = document.createElement('li');
					listItem.classList.add("notification-item");
					listItem.id = `noti-id-${notification.notification_id}`;
					console.log("noti id ", listItem.id);

					let closeButton = document.createElement('button');
					closeButton.textContent = '❌';
					closeButton.classList.add('close-btn');
					closeButton.onclick = () => hideNotification(notification.notification_id, listItem);

					let messageElement = document.createElement('p');
					messageElement.classList.add('noti-text-black');
					messageElement.textContent = `${notification.message}`;

					listItem.appendChild(closeButton);
					listItem.appendChild(messageElement);

					if (!notification.message.includes("hired") && !notification.message.includes("reported") && role === "caretaker" && !notification.message.includes("feedback")) {
						let openButton = document.createElement('button');
						openButton.textContent = "Open"; // New button to check work
						openButton.classList.add("work-confirmation-open");
						openButton.setAttribute("data-rid", notification.rId); // ✅ Attach rId

						openButton.onclick = function() {
							notiId = listItem.id;
							console.log("Notification ID:", "  ", notiId, " noti rid", notification.rId);

							checkRequestStatus(notification.rId);
						};
						listItem.appendChild(openButton);

					}

					notificationItems.appendChild(listItem);
				});
			}

			if (!notificationItems.hasChildNodes()) {
				let noNotificationMessage = document.createElement('p');
				noNotificationMessage.textContent = "No Notifications";
				noNotificationMessage.classList.add('no-notifications', 'noti-text-black');
				notificationItems.appendChild(noNotificationMessage);
			}
		})
		.catch(error => console.error('Error fetching notifications:', error));
}

function hideNotification(notificationId, listItem) {
	fetch(`${origin}/projectWithFullStack/hideNotification`, {
		method: "POST",
		headers: { "Content-Type": "application/x-www-form-urlencoded" },
		body: `notification_id=${notificationId}`
	})
		.then(response => response.json())
		.then(data => {
			if (data.success) {
				listItem.remove();
				decreaseNotificationCount();
			} else {
				alert("Failed to hide notification.");
			}
		})
		.catch(error => console.error("Error hiding notification:", error));
}

function decreaseNotificationCount() {
	const countBadge = document.querySelector('#bellIcon .count-badge');
	if (countBadge) {
		let currentCount = parseInt(countBadge.textContent) || 0;
		countBadge.textContent = currentCount > 1 ? currentCount - 1 : '';
	}
}


function getRole() {
	console.log("role " + origin);
	fetch(origin + "/projectWithFullStack/getRole", {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' }
	})
		.then(response => response.text())
		.then(data => {
			role = data;
			console.log('Role  :  ', data);
			checkRole();
			render();
		})
		.catch(error => console.error('Error:', error));
}

function render() {
	console.log("Rendering...");
	if (role === "caretaker") {
		console.log("Inside caretaker condition");
		document.getElementById("none-req-ser").style.display = "none";
		document.getElementById("serviceButton").style.display = "flex";
		document.getElementById("careTakerButton").style.display = "none";
/*		getAllCareTakers();
*/	}
	if (role === "need-care") {
		document.querySelector(".bio-section").style.display = "none";
		document.querySelector(".services-section").style.display = "none";
		console.log("Inside need-care condition");
		document.getElementById("none-req-ser").style.display = "flex";
		document.getElementById("serviceButton").style.display = "none";
		document.getElementById("careTakerButton").style.display = "flex";
	}
}

//SERVICES ...

document.getElementById("serviceButton").addEventListener("click", function() {
	console.log("fetcccc")
	fetchServices();
});

function fetchServices() {
	console.log("ser : " + origin);
	fetch(origin + "/projectWithFullStack/service", {
		method: "POST"
	})
		.then(response => response.json())
		.then(data => {
			console.log("Fetched Data:", data); // Debugging log

			if (!Array.isArray(data)) {
				console.error("Expected an array but got:", data);
				return;
			}

			let serList = document.getElementById("ser-list");
			serList.innerHTML = ''; // Clear previous services

			data.forEach((service) => {
				console.log("service : ", service); // Log each service object to ensure it has the expected structure

				let cardDiv = document.createElement("div");
				cardDiv.classList.add("service-card");
				cardDiv.id = "service" + service.id; // Use the actual service ID

				let infoDiv = document.createElement("div");
				infoDiv.classList.add("service-info");

				let spanInfo = document.createElement("span");
				spanInfo.classList.add("title");
				spanInfo.innerText = service.tittle;  // Ensure 'title' is correctly spelled
				console.log("span ", spanInfo);

				infoDiv.appendChild(spanInfo);
				cardDiv.appendChild(infoDiv);

				let tagDiv = document.createElement("div");
				tagDiv.classList.add("tags");

				let categorySpan = document.createElement("span");
				let locationSpan = document.createElement("span");
				let availabilitySpan = document.createElement("span");

				categorySpan.classList.add("category");
				locationSpan.classList.add("location");
				availabilitySpan.classList.add("availability", "open");

				categorySpan.innerText = service.required;
				locationSpan.innerText = service.place;
				availabilitySpan.innerText = "open";

				tagDiv.appendChild(categorySpan);
				tagDiv.appendChild(locationSpan);
				tagDiv.appendChild(availabilitySpan);
				cardDiv.appendChild(tagDiv);

				// Add event listener to call servlet when clicked
				availabilitySpan.addEventListener("click", function() {
					console.log("Clicked on service:", service.id);
					fetch(`${origin}/projectWithFullStack/serviceDetails?serviceId=${service.id}`)
						.then(response => response.json())
						.then(detail => {
							document.querySelectorAll(".nav-item").forEach(ele => ele.disabled = true);
							document.querySelector(".services-container").style.display = "none";
							document.querySelector(".service-particular-fullbody").style.display = "block";

							createParticularServiceUI(detail);
							console.log("Service Details:", detail);
							// Do something with the response (maybe display in a modal)
						})
						.catch(error => console.error("Error fetching service details:", error));
				});

				serList.appendChild(cardDiv); // Append here instead of inside the event listener
			});
		})
		.catch(error => console.error("Error fetching services:", error));
}

function createParticularServiceUI(detail) {
	document.querySelector(".service-particular-profile-image").src = detail.profile;
	document.getElementById("service-particular-person-name").innerText = detail.requestedPersonName;
	document.querySelector(".service-particular-service-card h1").innerText = detail.tittle;
	document.querySelector(".service-particular-description").innerText = detail.description;
	document.querySelector(".service-particular-detail-row:nth-child(3) .service-particular-value").innerText = detail.required;

	// Updating amount
	document.querySelector(".service-particular-detail-row:nth-child(4) .service-particular-value").innerText = detail.amount;

	// Updating time
	document.querySelector(".service-particular-detail-row:nth-child(5) .service-particular-value").innerText = detail.time;

	// Updating address
	document.querySelector(".service-particular-address-box").innerText = detail.fullAddress;

	console.log("UI Updated Successfully! 🎉");
}


document.querySelectorAll(".sidebar-nav .nav-item").forEach(button => {
	button.addEventListener("click", function() {
		const selectedText = this.querySelector("span").innerText;
		if (selectedText === "Care takers") {
			getAllCareTakers();
			document.querySelector(".all-caretaker-container").style.display = "block";
		}
		if (selectedText !== "Care takers") {
			document.querySelector(".all-caretaker-container").style.display = "none";
		}
		if (selectedText === "Services") {
			document.querySelector(".services-container").style.display = "block";
			//document.querySelector(".edit-profile").style.display = "none";
		}
		if (selectedText !== "Services") {
			document.querySelector(".services-container").style.display = "none";
		}
	});
});


// Initialize Lucide icons
lucide.createIcons();

// Mobile menu functionality
const menuToggle = document.querySelector('.menu-toggle');
const sidebar = document.querySelector('.sidebar');
const overlay = document.querySelector('.mobile-overlay');
const app = document.querySelector('.app');

menuToggle.addEventListener('click', () => {
	app.classList.toggle('sidebar-open');
});

overlay.addEventListener('click', () => {
	app.classList.remove('sidebar-open');
});

// View switching functionality
const navItems = document.querySelectorAll('.nav-item');
const views = document.querySelectorAll('.view');

navItems.forEach(item => {
	item.addEventListener('click', () => {
		navItems.forEach(nav => nav.classList.remove('active'));
		item.classList.add('active');

		const targetView = item.dataset.view;
		views.forEach(view => {
			view.classList.remove('active');
			if (view.classList.contains(targetView)) {
				view.classList.add('active');
			}
		});

		app.classList.remove('sidebar-open');
	});
});


document.getElementById("logoutButton").addEventListener("click", function() {
	console.log("loooo");
	// Show the logout confirmation popup
	document.querySelector(".popup").style.display = "flex";
});

document.getElementById("noButton").addEventListener("click", function() {
	console.log("hooo");
	// Close the popup without logging out
	document.querySelector(".popup").style.display = "none";
});

document.getElementById("yesButton").addEventListener("click", function() {
	console.log("sjhe");

	// Send request to server to log out and clear session
	fetch(`${origin}/projectWithFullStack/logout`, { method: 'POST' })
		.then(response => {
			if (response.ok) {
				// Redirect to login page after logout
				window.location.href = 'loginForm.html';
			}
		})
		.catch(error => {
			console.error('Error during logout:', error);
		});
});



document.querySelector(".edit-profile-btn").addEventListener("click", () => {
	document.querySelector(".main-content").style.display = "none";
	document.querySelector(".profile-section").style.display = "none";
	document.querySelectorAll(".nav-item").forEach(ele => ele.disabled = true);
	document.querySelector(".edit-profile").style.display = "block";
})

document.querySelector(".menu-button").addEventListener("click", () => {
	document.querySelector(".main-content").style.display = "block";
	document.querySelector(".profile-section").style.display = "block";
	document.querySelectorAll(".nav-item").forEach(ele => ele.disabled = false);
	document.querySelector(".edit-profile").style.display = "none";
})

document.querySelector(".service-particular-i > i").addEventListener("click", () => {
	document.querySelector(".services-container").style.display = "block";
	document.querySelectorAll(".nav-item").forEach(ele => ele.disabled = false);
	document.querySelector(".service-particular-fullbody").style.display = "none";
})



let host = window.location.host;
let socket = null;
let currentReceiverId = "";
let currentSenderId = "";
let currentSenderName = "";
let currentReceiverName = "";

// Fetch logged-in user and initialize WebSocket connection
function fetchLoggedInUser() {
	console.log('Fetching logged-in user...');
	return fetch(`${origin}/projectWithFullStack/getLoggedInUser`)
		.then(response => {

			if (!response.ok) {
				throw new Error('Failed to fetch logged-in user');
			}
			return response.json();
		})
		.then(data => {
			console.log('Logged-in user data:', data);
			currentSenderId = data.userId;
			currentSenderName = data.name;
			console.log("current sender name logged in user is : ", currentSenderName, " current sender id ", currentSenderId);

			// Initialize WebSocket connection after getting logged-in user
			socket = new WebSocket("wss://" + host + "/projectWithFullStack/chat?user_id=" + currentSenderId);
			socket.onopen = function(event) {
				console.log("dtaaaaaa", data);
				console.log('Connected to the WebSocket server');
			};

			socket.onmessage = function(event) {
				console.log("Received message:", event);
				const message = JSON.parse(event.data);
				console.log("messs", message);

				let isOnline = message.isOnline;
				let nameFromMessage = message.userName;
				if (nameFromMessage === undefined) {
					nameFromMessage = message.sender_name;
					isOnline = true;
				}
				console.log("Online Status:", nameFromMessage, " - ", isOnline);
				// Convert message.userName to match the modified class name format
				let userClass = nameFromMessage.replaceAll(" ", '-');
				console.log("uc", userClass);

				const wholeuser = document.querySelector(`.users-list .${userClass}`);
				console.log("whole", wholeuser);

				// Ensure the user element exists
				if (wholeuser) {
					// Select the second div inside wholeuser
					let statusSpan = wholeuser.querySelectorAll("div")[1]; // Selecting the second div

					console.log("spppannn", statusSpan);

					if (statusSpan) {
						// Update availability status
						if (isOnline) {
							statusSpan.classList.remove('available-red-circle');
							statusSpan.classList.add('available-green-circle');
						} else {
							statusSpan.classList.remove('available-green-circle');
							statusSpan.classList.add('available-red-circle');
						}
					}
					wholeuser.appendChild(statusSpan);
					// }
					console.log("iii", currentReceiverName);

					// Handle displaying the chat message
					if (message.sender_name !== undefined && currentReceiverName !== "") {
						console.log("sns", message.sender_name);

						const messagesDiv = document.getElementById('messages');

						const newMessage = document.createElement('div');
						newMessage.classList.add("message-box");
						newMessage.classList.add("text-align-start");

						let messageText = document.createElement("p");

						let timeDiv = document.createElement("p");
						timeDiv.innerText = message.time;
						newMessage.appendChild(timeDiv);
						timeDiv.classList.add("timestamp-style");

						messageText.textContent = `${nameFromMessage}: ${message.content}`;
						newMessage.appendChild(messageText);
						messagesDiv.appendChild(newMessage);
						messagesDiv.scrollTop = messagesDiv.scrollHeight; // Scroll to the bottom

					}
				}
			};

			socket.onclose = function(event) {
				console.log("close ev ", event);

				console.log('WebSocket connection closed');
			};

			socket.onerror = function(event) {
				console.error('WebSocket error:', event);
			};

			return data.userId;
		})
		.catch(error => {
			console.error('Error fetching logged-in user:', error);
		});
}

// Fetch users excluding the logged-in user
function fetchUsersExceptLoggedIn(userId) {
	return fetch(`${origin}/projectWithFullStack/getUsersExceptLoggedIn?userId=${userId}`)
		.then(response => {
			if (!response.ok) {
				throw new Error('Failed to fetch users list');
			}
			return response.json();
		})
		.then(users => {
			return users;
		})
		.catch(error => {
			console.error('Error fetching users:', error);
		});
}

// Fetch chat history between users
function fetchChatHistory(senderId, receiverId) {
	return fetch(`${origin}/projectWithFullStack/getChatHistory?senderId=${senderId}&receiverId=${receiverId}`)
		.then(response => {
			if (!response.ok) {
				throw new Error('Failed to fetch chat history');
			}
			return response.json();
		})
		.then(messages => {
			return messages;
		})
		.catch(error => {
			console.error('Error fetching chat history:', error);
		});
}

// Get logged-in user and populate the user list
fetchLoggedInUser()
	.then(userId => {
		console.log("sanddd")
		if (userId) {
			fetchUsersExceptLoggedIn(userId)
				.then(users => {
					const usersListDiv = document.getElementById('usersList');
					usersListDiv.innerHTML = '';  // Clear previous list
					if (users != undefined) {
						users.forEach(user => {
							let wholeuser = document.createElement("div");
							wholeuser.classList.add("username-btn");

							// Convert spaces to dashes to avoid invalid class names
							let userClass = user.name.replace(/\s+/g, '-');
							wholeuser.classList.add(userClass);

							let nameAndProfile = document.createElement('div');
							nameAndProfile.classList.add('name-profile');
							const userItem = document.createElement('h2');
							let span = document.createElement("div");
							userItem.classList.add('user-item');

							let profileImage = document.createElement('img');
							profileImage.src = user.profile;


							// Display availability status
							if (user.isAvailable) {
								span.classList.add('available-green-circle');
							} else {
								span.classList.add('available-red-circle');
							}

							// Display username
							userItem.innerHTML = `${user.name}`;
							wholeuser.onclick = function() {
								currentReceiverId = user.userId;
								loadChatHistory(user.userId);
							};
							profileImage.classList.add(`${user.name}`, "others-profile");

							nameAndProfile.appendChild(profileImage);
							nameAndProfile.appendChild(userItem);

							wholeuser.appendChild(nameAndProfile);
							wholeuser.appendChild(span);
							usersListDiv.appendChild(wholeuser);
						});
					}

				});
		}
	})
	.catch(error => console.error('Error during user fetch:', error));

// Load chat history when a user is selected
function loadChatHistory(receiverId) {
	fetchChatHistory(currentSenderId, receiverId)
		.then(messages => {
			const messagesDiv = document.getElementById('messages');
			messagesDiv.innerHTML = '';  // Clear previous messages
			console.log("sendId", currentSenderId, "  recId", currentReceiverId);
			console.log("receiveid", receiverId);

			messages.forEach(message => {
				currentReceiverName = message.senderName;
				console.log("history mes", message);
				let timeStamp = message.timestamp;
				console.log("time", timeStamp);
				const messageDiv = document.createElement('div');
				let messageText = document.createElement("p");
				let timeDiv = document.createElement("p");

				timeDiv.innerText = timeStamp;
				timeDiv.classList.add("timestamp-style"); // Add this for styling
				if (message.senderName === currentSenderName) {
					// console.log("equals");
					messageText.textContent = `You : ${message.content}`;
					messageDiv.classList.add("text-align-end");
				}
				else {
					messageText.textContent = `${message.senderName}: ${message.content}`;
					// console.log("not equlas");
					messageDiv.classList.add("text-align-start");
				}
				messageDiv.classList.add(currentReceiverName);
				messageDiv.appendChild(timeDiv);
				messageDiv.appendChild(messageText);
				messageDiv.classList.add("message-box");
				messagesDiv.appendChild(messageDiv);
			});
		})
		.catch(error => console.error('Error loading chat history:', error));
}

// Send message when the button is clicked
document.getElementById('sendMessageBtn').onclick = function() {
	let currentTime = getFormattedDateTime();
	const messageInput = document.getElementById('messageInput');
	const messageContent = messageInput.value.trim();

	if (!currentReceiverId) {
		alert("Please select a user to chat with!");
		return;
	}
	console.log("recename", currentReceiverName);

	if (messageContent !== '') {
		const message = {
			sender_id: currentSenderId,
			sender_name: currentSenderName,
			receiver_id: currentReceiverId,
			receiver_name: currentReceiverName,
			content: messageContent,
			time: currentTime
		};
		const messagesDiv = document.getElementById('messages');

		const messageDiv = document.createElement('div');
		messageDiv.classList.add("text-align-end", "message-box");

		let timeDiv = document.createElement("p");
		timeDiv.classList.add("timestamp-style");
		timeDiv.innerText = currentTime;
		messageDiv.appendChild(timeDiv);

		let newMessage = document.createElement("p");
		newMessage.innerText = `You : ${message.content}`;
		messageDiv.appendChild(newMessage);

		messagesDiv.appendChild(messageDiv);
		messagesDiv.scrollTop = messagesDiv.scrollHeight;
		socket.send(JSON.stringify(message));
		messageInput.value = ''; // Clear the input field
	}
};

function getFormattedDateTime() {
	const now = new Date();

	const year = now.getFullYear();
	const month = String(now.getMonth() + 1).padStart(2, '0');
	const day = String(now.getDate()).padStart(2, '0');
	const hours = String(now.getHours()).padStart(2, '0');
	const minutes = String(now.getMinutes()).padStart(2, '0');
	const seconds = String(now.getSeconds()).padStart(2, '0');

	return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

console.log(getFormattedDateTime()); // Example Output: 2025-02-13 10:19:14

// Optional: Send message when Enter key is pressed
document.getElementById('messageInput').addEventListener('keydown', function(event) {
	if (event.key === 'Enter') {
		document.getElementById('sendMessageBtn').click();
	}
});

/*  document.getElementById('chat').addEventListener("click",function(){
   	
  });*/


/*Start of videocall js*/

let videoCallLocalStream;
let videoCallRemoteStream;
let videoCallPeerConnection;
let videoCallUsername = "";
let videoCallPeerUsername = "";
let videoCallSocket;
let videoCallIsInCall = false;

const videoCallServers = {
	iceServers: [
		{ urls: ["stun:bn-turn2.xirsys.com"] },
		{
			username:
				"lHGk7pPvj47UprbUTK82ZDOK6pmGKvc-_YCEAdyOM9WfZZ9qg3ud56JwUjoOizwnAAAAAGe3JtBzYW5k",
			credential: "4bbf0dcc-ef8a-11ef-8790-0242ac140004",
			urls: [
				"turn:bn-turn2.xirsys.com:80?transport=udp",
				"turns:bn-turn2.xirsys.com:443?transport=tcp",
			],
		},
	],
};

const videoCallLocalVideo = document.getElementById("video-call-local");
const videoCallRemoteVideo = document.getElementById("video-call-remote");
const videoCallUsernameElement = document.getElementById("video-call-username");
const videoCallPeerElement = document.getElementById("video-call-peer");
const videoCallStartBtn = document.getElementById("video-call-start");
const videoCallEndBtn = document.getElementById("video-call-end");
const videoCallMuteVideoBtn = document.getElementById("video-call-mute-video");
const videoCallMuteAudioBtn = document.getElementById("video-call-mute-audio");
const videoCallIncomingModal = document.getElementById(
	"video-call-incoming-modal"
);
const videoCallCallerName = document.getElementById("video-call-caller-name");
const videoCallAcceptBtn = document.getElementById("video-call-accept");
const videoCallDeclineBtn = document.getElementById("video-call-decline");
const videoCallMessageModal = document.getElementById(
	"video-call-message-modal"
);
const videoCallMessageTitle = document.getElementById(
	"video-call-message-title"
);
const videoCallMessageText = document.getElementById("video-call-message-text");
const videoCallMessageIcon = document.getElementById("video-call-message-icon");
const videoCallMessageOkBtn = document.getElementById("video-call-message-ok");

function videoCallShowMessage(title, message, type = "info") {
	const icons = {
		success: "✅",
		error: "❌",
		info: "ℹ️",
		warning: "⚠️",
	};

	videoCallMessageTitle.textContent = title;
	videoCallMessageText.textContent = message;
	videoCallMessageIcon.textContent = icons[type];
	videoCallMessageModal.style.display = "flex";
}

videoCallMessageOkBtn.addEventListener("click", () => {
	videoCallMessageModal.style.display = "none";
	if (videoCallMessageText.innerText.trim().includes("ended")) {
		console.log("dddddd")
		location.reload();
	}
});

function videoCallInitialize() {
	videoCallPeerUsername = document.getElementById(
		"service-particular-person-name"
	).innerText;
	fetch(`${window.location.origin}/projectWithFullStack/getLoggedInUser`)
		.then((response) => response.json())
		.then((data) => {
			videoCallUsername = data.name;
			if (role === "caretaker") {
				console.log("ins else");
				// videoCallPeerUsername = document.getElementById(
				//     "service-particular-person-name"
				// ).innerText;
			} else {
				videoCallPeerUsername = caretakerName1;
			}
			console.log("cjjddd", videoCallPeerUsername);

			videoCallUsernameElement.innerText = `Username: ${videoCallUsername}`;
			videoCallPeerElement.innerText = `Peer: ${videoCallPeerUsername}`;
			videoCallConnectWebSocket();
		})
		.catch((error) => {
			console.error("Error fetching username:", error);
			videoCallShowMessage(
				"Error",
				"Failed to initialize the application",
				"error"
			);
		});
}

function videoCallConnectWebSocket() {
	videoCallSocket = new WebSocket(
		`wss://${window.location.host}/projectWithFullStack/ws/${videoCallUsername}`
	);

	videoCallSocket.onopen = () => console.log("WebSocket connected!");
	videoCallSocket.onerror = (error) => {
		console.error("WebSocket error:", error);
		videoCallShowMessage(
			"Connection Error",
			"Failed to connect to the server",
			"error"
		);
	};
	videoCallSocket.onclose = () => console.log("WebSocket closed!");

	videoCallSocket.onmessage = (event) => {
		const data = JSON.parse(event.data);

		// if (data.type === "call-request") {
		//   console.log("is call req from: ", data.from);
		//   videoCallPeerElement.innerText = `Peer: ${data.from}`;
		//   videoCallPeerUsername = data.from;
		// } else {
		//   console.log("is call req tooo: ", data.to);
		//   videoCallPeerElement.innerText = `Peer: ${
		//     document.getElementById("service-particular-person-name").innerText
		//   }`;
		//   videoCallPeerUsername = document.getElementById(
		//     "service-particular-person-name"
		//   ).innerText;
		// }

		console.log("onmessage", data, "from", data.from, "to", data.to);

		console.log("videoCallPeerUsername   ", videoCallPeerUsername);

		switch (data.type) {
			case "call-request":
				console.log("is call req from: ", data.from);
				videoCallPeerElement.innerText = `Peer: ${data.from}`;
				videoCallPeerUsername = data.from;
				videoCallShowIncomingCall(data.from);
				break;
			case "call-accepted":
				// console.log
				document.getElementById("video-call-message-modal").style.display =
					"none";
				videoCallStart(true);
				break;
			case "call-rejected":
				videoCallHandleRejected();
				break;
			case "offer":
				console.log("is call req tooo: ", data.to);
				console.log("OFFER OODA DATA: ", data)
				videoCallPeerElement.innerText = `Peer: ${data.from}`;
				videoCallPeerUsername = data.from;
				console.log("insss offere    ", videoCallPeerUsername);
				videoCallHandleOffer(data);
				break;
			case "answer":
				console.log("inss answer");
				videoCallHandleAnswer(data);
				break;
			case "ice-candidate":
				videoCallHandleICECandidate(data);
				break;
			case "hangup":
				videoCallHandleHangup();
				// location.reload(); // Reload after 1 second
				break;


		}
	};
}

function videoCallShowIncomingCall(caller) {
	console.log("callllerrr  ", caller);

	videoCallCallerName.textContent = `Incoming call from ${caller}`;
	videoCallIncomingModal.style.display = "flex";
}

function videoCallHideIncomingCall() {
	videoCallIncomingModal.style.display = "none";
}

function videoCallHandleRejected() {
	videoCallShowMessage(
		"Call Rejected",
		"The other person declined your call",
		"error"
	);
	videoCallResetState();
}

function videoCallCreatePeerConnection() {
	return new Promise((resolve) => {
		videoCallPeerConnection = new RTCPeerConnection(videoCallServers);
		videoCallRemoteStream = new MediaStream();
		videoCallRemoteVideo.srcObject = videoCallRemoteStream;

		videoCallLocalStream.getTracks().forEach((track) => {
			videoCallPeerConnection.addTrack(track, videoCallLocalStream);
		});

		videoCallPeerConnection.ontrack = (event) => {
			event.streams[0].getTracks().forEach((track) => {
				videoCallRemoteStream.addTrack(track);
			});
		};

		videoCallPeerConnection.onicecandidate = (event) => {
			if (event.candidate && videoCallSocket.readyState === WebSocket.OPEN) {
				videoCallSocket.send(
					JSON.stringify({
						type: "ice-candidate",
						to: videoCallPeerUsername,
						from: videoCallUsername,
						candidate: event.candidate,
					})

				);
			}
		};

		resolve(videoCallPeerConnection);
	});
}

function videoCallStart(isAccepting = false) {
	navigator.mediaDevices
		.getUserMedia({ video: true, audio: true })
		.then((stream) => {
			videoCallLocalStream = stream;
			console.log("videocallstream: ", stream);
			videoCallLocalVideo.srcObject = videoCallLocalStream;

			return videoCallCreatePeerConnection();
		})
		.then((pc) => {
			if (!isAccepting) {
				videoCallSocket.send(
					JSON.stringify({
						type: "call-request",
						to: videoCallPeerUsername,
						from: videoCallUsername,
					})
				);
				videoCallShowMessage(
					"Calling...",
					`Waiting for ${videoCallPeerUsername} to accept`,
					"info"
				);
			} else {
				return pc.createOffer().then((offer) => {
					console.log("OFFER: ", offer);
					return pc
						.setLocalDescription(offer)

						.then(() => {
							videoCallSocket.send(
								JSON.stringify({
									type: "offer",
									to: videoCallPeerUsername,
									from: videoCallUsername,
									offer: offer,
								})
							);
						});
				});
			}
		})
		.then(() => {
			videoCallShowControls();
		})
		.catch((error) => {
			console.error("Error starting call:", error);
			videoCallShowMessage(
				"Error",
				"Could not start call. Please check your camera and microphone permissions.",
				"error"
			);
		});
}

function videoCallHandleOffer(data) {
	let mediaPromise;

	console.log("MEDIAPROMISE: ", mediaPromise);
	if (!videoCallLocalStream) {
		mediaPromise = navigator.mediaDevices
			.getUserMedia({ video: true, audio: true })

			.then((stream) => {
				videoCallLocalStream = stream;
				videoCallLocalVideo.srcObject = videoCallLocalStream;
			});
	} else {
		mediaPromise = Promise.resolve();
	}

	mediaPromise
		.then(() => videoCallCreatePeerConnection())
		.then((pc) => {
			return pc
				.setRemoteDescription(new RTCSessionDescription(data.offer))
				.then(() => pc.createAnswer())
				.then((answer) => {
					console.log("ANSWER: ", answer);

					return pc.setLocalDescription(answer).then(() => {
						videoCallSocket.send(
							JSON.stringify({
								type: "answer",
								to: videoCallPeerUsername,
								from: videoCallUsername,
								answer: answer,
							})
						);
					});
				});
		})
		.then(() => {
			videoCallShowControls();
		})
		.catch((error) => {
			console.error("Error handling offer:", error);
			videoCallShowMessage("Error", "Failed to establish connection", "error");
		});
}

function videoCallHandleAnswer(data) {
	console.log("dddaaattaa", data);
	videoCallPeerConnection
		.setRemoteDescription(new RTCSessionDescription(data.answer))
		.catch((error) => {
			console.error("Error handling answer:", error);
			videoCallShowMessage("Error", "Failed to establish connection", "error");
		});
}

function videoCallHandleICECandidate(data) {
	if (videoCallPeerConnection) {
		console.log("VideoCallPeerConnection: ", videoCallPeerConnection);
		videoCallPeerConnection
			.addIceCandidate(new RTCIceCandidate(data.candidate))
			.catch((error) => {
				console.error("Error handling ICE candidate:", error);
			});
	}
}

function videoCallHandleHangup() {
	if (videoCallLocalStream) {
		videoCallLocalStream.getTracks().forEach(track => track.stop()); // Stop webcam & mic
		videoCallLocalStream = null;
	}
	videoCallLocalVideo.srcObject = null; // Clear the video element

	videoCallShowMessage("Call Ended", "The call has been ended", "info");
	videoCallResetState();
}

function videoCallShowControls() {
	videoCallIsInCall = true;
	videoCallStartBtn.style.display = "none";
	videoCallEndBtn.style.display = "inline-block";
	videoCallMuteVideoBtn.style.display = "inline-block";
	videoCallMuteAudioBtn.style.display = "inline-block";
}

function videoCallResetState() {
	if (videoCallPeerConnection) {
		videoCallPeerConnection.close();
		videoCallPeerConnection = null;
	}
	if (videoCallLocalStream) {
		videoCallLocalStream.getTracks().forEach((track) => track.stop());
		videoCallLocalStream = null;
	}
	if (videoCallRemoteStream) {
		videoCallRemoteStream.getTracks().forEach((track) => track.stop());
		videoCallRemoteStream = null;
	}

	videoCallLocalVideo.srcObject = null;
	videoCallRemoteVideo.srcObject = null;

	videoCallIsInCall = false;
	videoCallStartBtn.style.display = "inline-block";
	videoCallEndBtn.style.display = "none";
	videoCallMuteVideoBtn.style.display = "none";
	videoCallMuteAudioBtn.style.display = "none";
	videoCallHideIncomingCall();
}

// Event Listeners
videoCallStartBtn.addEventListener("click", () => videoCallStart(false));

videoCallEndBtn.addEventListener("click", () => {
	location.reload(); // Reload after 1 second
	if (!videoCallLocalStream) {
		mediaPromise = navigator.mediaDevices
			.getUserMedia({ video: false, audio: false })

			.then((stream) => {
				videoCallLocalStream = stream;
				videoCallLocalVideo.srcObject = videoCallLocalStream;
			});


	} else {
		mediaPromise = Promise.resolve();
	}


	videoCallSocket.send(
		JSON.stringify({
			type: "hangup",
			to: videoCallPeerUsername,
			from: videoCallUsername
		})
	);
	videoCallResetState();
});

videoCallMuteVideoBtn.addEventListener("click", () => {
	const videoTrack = videoCallLocalStream.getVideoTracks()[0];
	videoTrack.enabled = !videoTrack.enabled;
	videoCallMuteVideoBtn.textContent = videoTrack.enabled
		? "Mute Video"
		: "Unmute Video";
});

videoCallMuteAudioBtn.addEventListener("click", () => {
	const audioTrack = videoCallLocalStream.getAudioTracks()[0];
	audioTrack.enabled = !audioTrack.enabled;
	videoCallMuteAudioBtn.textContent = audioTrack.enabled
		? "Mute Audio"
		: "Unmute Audio";
});

videoCallAcceptBtn.addEventListener("click", () => {
	console.log("ins accept");
	videoCallHideIncomingCall();
	document.querySelector(".video-call-body").style.top = 0;
	document.querySelector(".video-call-body").style.left = 0;
	document.querySelector(".video-call-body").style.right = 0;
	console.log("ppppppeeee    ", videoCallPeerUsername);

	videoCallSocket.send(
		JSON.stringify({
			type: "call-accepted",
			to: videoCallPeerUsername,
			from: videoCallCallerName
		})
	);
});

videoCallDeclineBtn.addEventListener("click", () => {
	if (!videoCallLocalStream) {
		mediaPromise = navigator.mediaDevices
			.getUserMedia({ video: false, audio: false })

			.then((stream) => {
				videoCallLocalStream = stream;
				videoCallLocalVideo.srcObject = videoCallLocalStream;
			});
	} else {
		mediaPromise = Promise.resolve();
	}
	console.log("decline")
	videoCallHideIncomingCall();
	videoCallSocket.send(
		JSON.stringify({
			type: "call-rejected",
			to: videoCallPeerUsername,
			from: videoCallUsername
		})
	);
});

// Initialize the application
// videoCallInitialize();
/*end of videocall js*/



document.querySelector(".save-info-btn").addEventListener("click", function() {
	// Get input values
	let fullName = document.getElementById("full-name").value.trim();
	let email = document.querySelector("#set-email input").value.trim();
	let mobile = document.getElementById("edit_mobile").value.trim();
	let bio = document.getElementById("edit-profile-bio-textarea").value.trim();
	let gender = document.querySelector('input[name="gender"]:checked').value;

	// Get checked services
	let services = [];
	document.querySelectorAll('.services-section input[type="checkbox"]:checked').forEach(checkbox => {
		services.push(checkbox.value);
	});
	let servicesInterested = services.join(", "); // Convert array to comma-separated string

	// Change password (optional)
	let changePassword = document.getElementById("cp").value.trim();

	// Validation checks
	if (!fullName) {
		alert("Full Name is required!");
		return;
	}
	if (!email) {
		alert("Email Address is required!");
		return;
	}
	if (!mobile) {
		alert("Mobile Number is required!");
		return;
	}
	if (role === "caretaker") {
		if (!bio) {
			alert("Bio is required!");
			return;
		}
		if (services.length === 0) {
			alert("At least one Service must be selected!");
			return;
		}
	}

	// Create JSON object
	let requestData = {
		fullName: fullName,
		email: email,
		mobile: mobile,
		gender: gender,
		bio: role === "caretaker" ? bio : "",
		servicesInterested: role === "caretaker" ? servicesInterested : "",
		changePassword: changePassword // Will be empty if not provided
	};

	sendToServlet(requestData);
});
// Function to send data to the servlet
function sendToServlet(data) {
	fetch(`${origin}/projectWithFullStack/UpdateAllDetails`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(data)
	})
		.then(response => response.json())  // Convert response to JSON
		.then(result => {
			if (!result.message.includes("Please")) {
				fetchDetailsForDashboard();
			}
			alert(result.message);
			console.log("Success:", result); // Log success message
		})
		.catch(error => console.error("Error:", error)); // Log any errors
}


// function call goes here...

document.addEventListener("DOMContentLoaded", function() {
	console.log("sandddddd");
	videoCallInitialize();
});
loadJobsFromDatabase(); // Load jobs

fetchProfile();
fetchDetailsForDashboard();
fetchAddressDetails();
loadUserInfo();
fetchNotificationCount();
getRole();
