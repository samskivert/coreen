;; coreen.el --- bindings to the Coreen code reading environment.
;;
;; Copyright (C) 2010-2011 Michael Bayne
;;
;; Author: Michael Bayne <mdb * samskivert com>
;; Version: 1.0
;; URL: http://github.com/samskivert/coreen
;; Compatibility: GNU Emacs 22.x, GNU Emacs 23.x
;;
;; This file is NOT part of GNU Emacs.
;;
;;; Commentary:
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
;; (add-hook 'java-mode-hook 'coreen-java-mode-hook);
;;
;;; Code:

(defvar coreen-url "http://localhost:8192/coreen"
  "The URL via which we communicate with Coreen.")
(defvar coreen-marker-ring (make-ring 16)
  "Ring of markers which are locations from which \\[coreen-open-symbol] was invoked.")

;; Used to handle next- and prev-error when navigating through results
(defvar coreen-error-pos nil)
(make-variable-buffer-local 'coreen-error-pos)

;; Used when querying Coreen and processing the results
(defvar coreen-buffer-name "*coreen*")
(defvar coreen-searched-sym nil)

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
    ;; TODO: don't use curl, use Emacs to fetch the URL (maybe url-retrieve-synchronously?)
    (let* ((sym (thing-at-point 'symbol))
           (command (concat "curl -s '" coreen-url "/service?action=def"
			    "&src=" (buffer-file-name)
			    "&pos=" (number-to-string (- (point) 1))
                            "&sym=" sym
			    "'"))
           (buffer (get-buffer-create coreen-buffer-name)))
      (setq coreen-searched-sym sym)
      (shell-command command buffer)
      (setq next-error-last-buffer buffer)
      (let ((rcount (with-current-buffer buffer
                      (coreen-results-mode)
                      (goto-char 0)
                      (count-lines (point-min) (point-max))
                      )))
        (message (format "Coreen found %d result(s)." rcount))
        (coreen-next-error-function 0 nil)))))

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

;;; implementation details

(define-derived-mode coreen-results-mode nil "coreen"
  "Major mode for Coreen output."
  (setq next-error-function 'coreen-next-error-function coreen-error-pos nil))

(defun coreen-next-error-function (arg reset)
  (let ((savepoint (point-marker)))
    (with-current-buffer (get-buffer coreen-buffer-name)
      ;; position the point on the desired result (in the coreen results buffer)
      (if reset
          ;; if we're resetting, just go to the first result
          (goto-char 0)
        ;; otherwise we'll be moving foward or backward based on the value of arg
        (progn
          ;; handle wrapping back to the end if we're at the first result and arg < 0
          (when (and (eq 1 (line-number-at-pos)) (< arg 0))
            (message "Start of matches (wrapped).")
            ;; move to the end of the buffer; below we will back up one line
            ;; and end up at the right place
            (goto-char (point-max)))
          ;; now move forward (or backward) the requested number of results (lines)
          (forward-line arg)
          ;; if we ended up on the last line (which is blank) then wrap back
          ;; around to the first result
          (when (null (thing-at-point 'symbol))
            (message "End of matches (wrapped).")
            (goto-char 0))))
      ;; now process the result on the current line
      (let ((toks (split-string (thing-at-point 'line))))
        (cond ((string= (car toks) "nomatch")
               (message "Could not locate symbol: %s" coreen-searched-sym))
              ((string= (car toks) "match")
               (ring-insert coreen-marker-ring savepoint) ;; record whence we came
               (find-file (cadr toks))
               (goto-char (+ (string-to-number (caddr toks)) 1))
               )
              (t (message (substring result 0 -1))) ;; strip newline
              )))))
  
(provide 'coreen)
;;; coreen.el ends here
