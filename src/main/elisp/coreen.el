;;
;; Defines various functions for interacting with Coreen.

(defconst coreen-url "http://localhost:8192/coreen"
  "The URL via which we communicate with Coreen.")
(defvar coreen-marker-ring (make-ring 16)
  "Ring of markers which are locations from which \\[coreen-open-symbol] was invoked.")

(defun coreen-find-symbol (class)
  "Searches Coreen for the symbol under the point. Only the text
of the symbol is used for searching, the current buffer need not
contain a compilation unit known to Coreen."
  (interactive (list (read-from-minibuffer "Symbol: " (thing-at-point 'symbol))))
  (browse-url (concat coreen-url "/#LIBRARY~search~" class))
  )

(defun coreen-view-symbol ()
  "Views the symbol under the point in Coreen. The current buffer
must contain a compilation that has been processed by Coreen."
  (interactive)
  (browse-url (concat coreen-url "/service?action=view"
                      "&src=" (buffer-file-name)
                      "&pos=" (number-to-string (point))
                      "&sym=" (thing-at-point 'symbol)))
  )

(defun coreen-open-symbol ()
  "Navigates to the symbol under the point."
  (interactive)
  ;; TODO: don't use GET, use Emacs to fetch the URL (maybe url-retrieve-synchronously?)
  (let* ((command (concat "GET '" coreen-url "/service?action=resolve"
                          "&src=" (buffer-file-name)
                          "&pos=" (number-to-string (point))
                          ;; TODO: append &sym=cursym?
                          "'"))
         (output (split-string (shell-command-to-string command))))
    (if (string= (car output) "nomatch")
        (message "Could not locate symbol: %s." (thing-at-point 'symbol))
      (ring-insert coreen-marker-ring (point-marker)) ;; Record whence we came.
      (find-file (car (cdr output)))
      (goto-char (string-to-number (car (cdr (cdr output)))))
      )
    ))

(defun pop-coreen-mark ()
  "Pop back to where \\[coreen-open-symbol] was last invoked."
  (interactive)
  (if (ring-empty-p coreen-marker-ring)
      (error "No previous locations for coreen-open-symbol invocation."))
  (let ((marker (ring-remove coreen-marker-ring 0)))
    (switch-to-buffer (or (marker-buffer marker)
                          (error "The marked buffer has been deleted.")))
    (goto-char (marker-position marker))
    (set-marker marker nil nil)))
