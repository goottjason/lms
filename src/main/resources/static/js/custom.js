document.addEventListener('DOMContentLoaded', function () {
    document.querySelector('.custom-file-input').addEventListener('change', function (e) {
        const fileName = e.target.files[0]?.name;
        const label = e.target.nextElementSibling;
        if (label && fileName) {
            label.textContent = fileName;
        }
    });
});