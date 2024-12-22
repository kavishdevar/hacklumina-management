document.addEventListener("DOMContentLoaded", function() {
    const tableViewButton = document.getElementById("view-table");
    const cardsViewButton = document.getElementById("view-cards");
    const tableView = document.getElementById("registrations-table");
    const cardsView = document.getElementById("registrations-cards");
    const modal = document.getElementById("status-modal");
    const closeModal = document.querySelector(".close");
    const statusForm = document.getElementById("status-form");
    const filterPending = document.getElementById("filter-pending");
    const filterConfirmed = document.getElementById("filter-confirmed");
    const filterCancelled = document.getElementById("filter-cancelled");
    const customSelectTrigger = document.querySelector(".custom-select-trigger");
    const customOptions = document.querySelectorAll(".custom-option");
    const statusInput = document.getElementById("status");
    const selectedStatus = document.getElementById("selected-status");
    let currentRegistrationId = null;

    const nfcModal = document.getElementById("nfc-modal");
    const tagIdInput = document.getElementById("tag-id");
    const nfcModalMessage = document.getElementById("nfc-modal-message");

    function writeNfcTag(tagId) {
        const ndef = new NDEFReader();
        ndef.write(tagId).then(() => {
            const loadingSymbol = document.getElementById("loadingSymbol");
            loadingSymbol.classList.add("done");
        }).catch(error => {
            const nfcModalMessage = document.getElementById("nfc-modal-message");
            nfcModalMessage.textContent = `Failed to write NFC tag: ${error}`;
        });
    }

    function generateRandomTagId() {
        return Math.random().toString(36).substr(2, 9);
    }

    function setViewMode(mode) {
        if (mode === "table") {
            tableView.style.display = "block";
            cardsView.style.display = "none";
        } else {
            cardsView.style.display = "flex";
            tableView.style.display = "none";
        }
        localStorage.setItem("viewMode", mode);
    }

    function applyFilters() {
        const showPending = filterPending.checked;
        const showConfirmed = filterConfirmed.checked;
        const showCancelled = filterCancelled.checked;

        document.querySelectorAll("[data-status]").forEach(element => {
            const status = element.dataset.status;
            if ((status === "Pending" && showPending) ||
                (status === "Confirmed" && showConfirmed) ||
                (status === "Cancelled" && showCancelled)) {
                element.classList.add("animated");
                element.style.display = "";
            } else {
                element.classList.add("fade-out");
                setTimeout(() => {
                    element.style.display = "none";
                    element.classList.remove("fade-out");
                }, 300);
            }
        });
    }

    tableViewButton.addEventListener("click", function() {
        setViewMode("table");
    });

    cardsViewButton.addEventListener("click", function() {
        setViewMode("cards");
    });

    document.querySelectorAll(".change-status").forEach(button => {
        button.addEventListener("click", function() {
            currentRegistrationId = this.closest("[data-id]").dataset.id;
            const currentStatus = this.closest("[data-id]").dataset.status;
            modal.classList.remove("close");
            modal.style.display = "flex";
            selectedStatus.textContent = currentStatus;
            statusInput.value = currentStatus;
        });
    });

    closeModal.addEventListener("click", function() {
        modal.classList.add("close");
        modal.style.display = "none";
    });

    modal.addEventListener("click", function(event) {
        if (event.target == modal) {
            modal.classList.add("close");
            modal.style.display = "none";
        }
    });

    statusForm.addEventListener("click", function(event) {
        event.stopPropagation();
    });

    statusForm.addEventListener("submit", function(event) {
        event.preventDefault();
        const status = statusInput.value;
        if (status === "Confirmed" && !tagIdInput.value) {
            tagIdInput.value = generateRandomTagId();
            return;
        }
        const tagId = tagIdInput.value;
        fetch(`/update-registration-status/${currentRegistrationId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ "status": status, "tag-id": tagId })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const element = document.querySelector(`[data-id="${currentRegistrationId}"]`);
                element.dataset.status = status;
                if (status === "Confirmed") {
                    nfcModal.classList.remove("close");
                    nfcModal.style.display = "flex";
                    const nfcModalMessage = document.getElementById("nfc-modal-message");
                    nfcModalMessage.innerHTML = '<div id="loadingSymbol"></div>';
                    writeNfcTag(tagId);
                }
                if ((status === "Pending" && filterPending.checked) ||
                    (status === "Confirmed" && filterConfirmed.checked) ||
                    (status === "Cancelled" && filterCancelled.checked)) {
                    element.classList.add("animated");
                } else {
                    element.classList.add("fade-out");
                    setTimeout(() => {
                        element.style.display = "none";
                        element.classList.remove("fade-out");
                    }, 300);
                }
                modal.classList.add("close");
                modal.style.display = "none";
            } else {
                alert("Failed to update status");
            }
        });
    });

    filterPending.addEventListener("change", applyFilters);
    filterConfirmed.addEventListener("change", applyFilters);
    filterCancelled.addEventListener("change", applyFilters);

    customSelectTrigger.addEventListener("click", function() {
        this.parentElement.classList.toggle("open");
    });

    customOptions.forEach(option => {
        option.addEventListener("click", function() {
            selectedStatus.textContent = this.textContent;
            statusInput.value = this.getAttribute("data-value");
            this.parentElement.parentElement.classList.remove("open");
        });
    });

    // Apply the preferred view mode and filters on page load
    const preferredViewMode = localStorage.getItem("viewMode") || "table";
    setViewMode(preferredViewMode);
    applyFilters();

    const toggleButton = document.getElementById('toggle-filters');
    const filtersContainer = document.getElementById('filters-container');

    toggleButton.addEventListener('click', function() {
        filtersContainer.classList.toggle('open');
    });
});