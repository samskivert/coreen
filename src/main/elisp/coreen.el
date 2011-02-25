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
;; Configure Coreen for use in Java mode by adding the following to your .emacs
;; file (customizing key bindings to your preference, naturally):
;;
;; (load "path/to/coreen")
;; (defun coreen-java-mode-hook ()
;;   (define-key java-mode-map "\C-c\C-j" 'coreen-view-symbol)
;;   (define-key java-mode-map "\M-."     'coreen-open-symbol)
;;   (define-key java-mode-map "\M-/"     'pop-coreen-mark)
;;   ;; these navigate between matches when there are multiple
;;   (define-key java-mode-map "\M-]"     'next-error)
;;   (define-key java-mode-map "\M-["     'previous-error)
;;   )
;; (add-hook 'java-mode-hook 'coreen-java-mode-hook);
;;
;;; Code:

(defvar coreen-url "http://localhost:8192/coreen"
  "The URL via which we communicate with Coreen.")
(defvar coreen-marker-ring (make-ring 16)
  "Ring of markers which are locations from which \\[coreen-open-symbol] was invoked.")

;; Used to handle next- and previous-error when navigating through results
(defvar coreen-error-pos nil)
(make-variable-buffer-local 'coreen-error-pos)

;; Used when querying Coreen and processing the results
(defvar coreen-buffer-name "*coreen*")
(defvar coreen-searched-sym nil)

(defun coreen-browse-url (url)
  "The function called by the Coreen bindings to display a URL.
The default implementation simply calls (browse-url url) but this
can be redefined to provide custom behavior."
  (browse-url url)
  )

(defun coreen-find-symbol (name)
  "Searches Coreen for all definitions with the same name as the
symbol under the point. The results are displayed in your web
browser."
  (interactive (list (read-from-minibuffer "Symbol: " (thing-at-point 'symbol))))
  (coreen-browse-url (concat coreen-url "/#LIBRARY~search~" name))
  )

(defun coreen-view-symbol (name)
  "Displays the symbol under the point in your web browser, using
Coreen. If Coreen is not able to uniquely identify the symbol
under the point, it will display all symbols with the same name
as the queried symbol."
  (interactive (list (read-from-minibuffer "Symbol: " (thing-at-point 'symbol))))
  (coreen-browse-url (concat coreen-url "/service?action=view"
                      "&src=" (buffer-file-name)
                      "&pos=" (number-to-string (- (point) 1))
                      "&sym=" name))
  )

(defun coreen-open-symbol (name)
  "Navigates to the definition of the symbol under the point. If
Coreen is not able to uniquely identify the symbol under the
point, it will return all symbols with the same name as the
queried symbol. These matches can be navigated using
\\[next-error] and \\[previous-error]]."
  (interactive (list (read-from-minibuffer "Symbol: " (thing-at-point 'symbol))))
  ;; TODO: don't use curl, use Emacs to fetch the URL (maybe url-retrieve-synchronously?)
  (let* ((command (concat "curl -s '" coreen-url "/service?action=def"
                          "&src=" (buffer-file-name)
                          "&pos=" (number-to-string (- (point) 1))
                          "&sym=" name
                          "'"))
         (buffer (get-buffer-create coreen-buffer-name)))
    (setq coreen-searched-sym name)
    (shell-command command buffer)
    (setq next-error-last-buffer buffer)
    (let ((rcount (with-current-buffer buffer
                    (coreen-results-mode)
                    (goto-char 0)
                    (count-lines (point-min) (point-max))
                    )))
      (message (format "Coreen found %d result(s)." rcount))
      (coreen-next-error-function 0 nil))))

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
      (let* ((result (thing-at-point 'line))
	     (toks (split-string result)))
        (cond ((string= result "")
	       (message "No response from Coreen.  Is Coreen running?"))
	      ((string= (car toks) "nomatch")
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
