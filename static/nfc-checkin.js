document.getElementById('start-scan').addEventListener('click', async () => {
    const field = document.getElementById('field').value;
    const nfcModal = document.getElementById('nfc-modal');
    const nfcModalMessage = document.getElementById('nfc-modal-message');
    const nfcModalClose = document.getElementById('nfc-modal-close');
    const closeModal = () => {
        nfcModal.classList.add('close');
        nfcModal.style.display = 'none';
    };

    nfcModalClose.addEventListener('click', closeModal);
    document.querySelector('.close').addEventListener('click', closeModal);

    try {
        const ndef = new NDEFReader();
        await ndef.scan();
        ndef.onreading = event => {
            const decoder = new TextDecoder();
            let tagId = '';
            for (const record of event.message.records) {
                if (record.recordType === 'text') {
                    tagId = decoder.decode(record.data);
                    break;
                }
            }
            fetch('/nfc-checkin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ tag_id: tagId, field: field })
            })
            .then(response => response.json())
            .then(data => {
                nfcModalMessage.textContent = data.success ? 'Check-in successful!' : `Check-in failed: ${data.error}`;
                nfcModal.classList.remove('close');
                nfcModal.style.display = 'flex';
            })
            .catch(error => {
                nfcModalMessage.textContent = `Error: ${error}`;
                nfcModal.classList.remove('close');
                nfcModal.style.display = 'flex';
            });
        };
    } catch (error) {
        nfcModalMessage.textContent = `NFC scanning not supported: ${error}`;
        nfcModal.classList.remove('close');
        nfcModal.style.display = 'flex';
    }
});