;;
;; Defines various functions for interacting with Coreen.
;;
;; You can wire these into your Java mode by adding the following to your
;; .emacs file:
;;
;; (load "path/to/coreen")
;; (defun coreen-java-mode-hook ()
;;   (define-key java-mode-map "\C-c\C-j" 'coreen-find-symbol)
;;   (define-key java-mode-map "\M-."     'coreen-open-symbol)
;;   (define-key java-mode-map "\M-/"     'pop-coreen-mark)
;;   (define-key java-mode-map "\M-?"     'coreen-view-symbol)
;;   )
;; (add-hook 'java-mode-hook 'coreen-java-mode-hook)

(defvar coreen-url "http://localhost:8192/coreen"
  "The URL via which we communicate with Coreen.")
(defvar coreen-marker-ring (make-ring 16)
  "Ring of markers which are locations from which \\[coreen-open-symbol] was invoked.")

(defun coreen-browse-url (url)
  "The function called by the Coreen bindings to display a URL. The default
  implementation simply calls (browse-url url) but this can be redefined to
  provide custom behavior."
  (browse-url url)
  )

(defun coreen-find-symbol (class)
  "Searches Coreen for the symbol under the point. Only the text
of the symbol is used for searching, the current buffer need not
contain a compilation unit known to Coreen."
  (interactive (list (read-from-minibuffer "Symbol: " (thing-at-point 'symbol))))
  (coreen-browse-url (concat coreen-url "/#LIBRARY~search~" class))
  )

(defun coreen-view-symbol ()
  "Views the symbol under the point in Coreen. The current buffer
must contain a compilation that has been processed by Coreen."
  (interactive)
  (coreen-browse-url (concat coreen-url "/service?action=view"
                      "&src=" (buffer-file-name)
                      "&pos=" (number-to-string (- (point) 1))
                      "&sym=" (thing-at-point 'symbol)))
  )

(defun coreen-open-symbol ()
  "Navigates to the symbol under the point."
  (interactive)
  (if (not (thing-at-point 'symbol))
      (message "There is no symbol under the point.")
    ;; TODO: don't use GET, use Emacs to fetch the URL (maybe url-retrieve-synchronously?)
    (let* ((command (concat "GET '" coreen-url "/service?action=resolve"
			    "&src=" (buffer-file-name)
			    "&pos=" (number-to-string (- (point) 1))
			    ;; TODO: append &sym=cursym?
			    "'"))
	   (result (shell-command-to-string command))
	   (result-words (split-string result)))
      (cond ((string= (car result-words) "nomatch")
             (message "Could not locate symbol: %s" (thing-at-point 'symbol)))
            ((string= (car result-words) "match")
	     (ring-insert coreen-marker-ring (point-marker)) ;; Record whence we came.
	     (find-file (car (cdr result-words)))
	     (goto-char (+ (string-to-number (car (cdr (cdr result-words)))) 1))
	     )
            (t (message (substring result 0 -1))) ;; strip newline
            ))))

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
