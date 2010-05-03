function confirmLink(msg) {
    if (typeof(window.opera) != 'undefined') {
        return true;
    }
    return confirm(msg);
}
