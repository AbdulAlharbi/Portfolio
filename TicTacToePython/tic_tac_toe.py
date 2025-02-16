# How to Run
# Ensure you have Python 3 installed.
# Copy the above code into a file named, e.g., tic_tac_toe.py.
# Run it:
# bash
# Copy
# Edit
# python tic_tac_toe.py
# A window will appear with a 3x3 grid. Click on any empty square to place an X or O (starting with X). The game detects wins and ties.
# Use the New Game button to clear the board (while keeping scores).
# Use Reset Scores to clear scoreboard counters.
# Press Quit (or close the window) to exit.

# WHat I want to maybe do next: 
# Adjust Colors/Fonts: Modify background colors, button colors, or fonts (e.g., bigger or fancier).
# Add AI?!: For a more advanced project, implement a simple AI for single-player mode.
# Animation: Use advanced tkinter techniques, like highlighting the winning line with a flash or changing background color.
# Larger Boards: Expand to 4×4 or more for a bigger challenge.

import tkinter as tk
from tkinter import messagebox

class TicTacToe:
    def __init__(self, master):
        self.master = master
        self.master.title("Tic Tac Toe")
        self.master.resizable(False, False)  # Prevent window resize to keep layout consistent

        # Track the current player ("X" starts)
        self.current_player = "X"

        # Scoreboard: dictionary to count wins
        self.scores = {"X": 0, "O": 0}

        # Create the main frame
        self.main_frame = tk.Frame(self.master, bg="#ECECEC", padx=20, pady=20)
        self.main_frame.pack()

        # Title label
        self.title_label = tk.Label(
            self.main_frame,
            text="TIC TAC TOE",
            font=("Helvetica", 24, "bold"),
            fg="#333333",
            bg="#ECECEC"
        )
        self.title_label.grid(row=0, column=0, columnspan=3, padx=5, pady=5)

        # Subtitle label / Current player label
        self.status_label = tk.Label(
            self.main_frame,
            text=f"Current Player: {self.current_player}",
            font=("Helvetica", 14, "italic"),
            fg="#333333",
            bg="#ECECEC"
        )
        self.status_label.grid(row=1, column=0, columnspan=3, padx=5, pady=(0, 15))

        # Board buttons (3x3)
        self.buttons = []
        for row in range(3):
            button_row = []
            for col in range(3):
                btn = tk.Button(
                    self.main_frame,
                    text="",
                    font=("Helvetica", 20, "bold"),
                    width=5,
                    height=2,
                    bg="#F0F0F0",
                    command=lambda r=row, c=col: self.handle_click(r, c)
                )
                btn.grid(row=row+2, column=col, padx=5, pady=5)
                button_row.append(btn)
            self.buttons.append(button_row)

        # Scoreboard area (bottom)
        self.score_frame = tk.Frame(self.main_frame, bg="#ECECEC")
        self.score_frame.grid(row=5, column=0, columnspan=3, pady=(15, 5))

        self.x_score_label = tk.Label(
            self.score_frame,
            text=f"X Score: {self.scores['X']}",
            font=("Helvetica", 12, "bold"),
            bg="#ECECEC",
            fg="#333333"
        )
        self.x_score_label.pack(side=tk.LEFT, padx=20)

        self.o_score_label = tk.Label(
            self.score_frame,
            text=f"O Score: {self.scores['O']}",
            font=("Helvetica", 12, "bold"),
            bg="#ECECEC",
            fg="#333333"
        )
        self.o_score_label.pack(side=tk.LEFT, padx=20)

        # Buttons to reset board or reset scores
        self.button_frame = tk.Frame(self.main_frame, bg="#ECECEC")
        self.button_frame.grid(row=6, column=0, columnspan=3, pady=10)

        self.reset_button = tk.Button(
            self.button_frame,
            text="New Game",
            font=("Helvetica", 12, "bold"),
            bg="#B2DFFC",
            fg="black",
            command=self.reset_board
        )
        self.reset_button.pack(side=tk.LEFT, padx=10)

        self.reset_score_button = tk.Button(
            self.button_frame,
            text="Reset Scores",
            font=("Helvetica", 12, "bold"),
            bg="#FFBDBD",
            fg="black",
            command=self.reset_scores
        )
        self.reset_score_button.pack(side=tk.LEFT, padx=10)

        # Quit button
        self.quit_button = tk.Button(
            self.button_frame,
            text="Quit",
            font=("Helvetica", 12, "bold"),
            bg="#FFD699",
            fg="black",
            command=self.master.quit
        )
        self.quit_button.pack(side=tk.LEFT, padx=10)

    def handle_click(self, row, col):
        """Handle a player's move when they click a board button."""
        btn = self.buttons[row][col]
        # If button is already used, ignore
        if btn["text"] != "":
            return

        # Set the button text to the current player's mark
        btn["text"] = self.current_player

        # Check for a winner or tie
        if self.check_winner(self.current_player):
            messagebox.showinfo("Game Over", f"Player {self.current_player} wins!")
            self.scores[self.current_player] += 1
            self.update_score_labels()
            self.reset_board()
            return
        elif self.is_board_full():
            messagebox.showinfo("Game Over", "It's a tie!")
            self.reset_board()
            return

        # Switch player
        self.current_player = "O" if self.current_player == "X" else "X"
        self.status_label.config(text=f"Current Player: {self.current_player}")

    def check_winner(self, player):
        """Check if 'player' has won the game."""
        # Check rows
        for row in range(3):
            if (self.buttons[row][0]["text"] == player and
                self.buttons[row][1]["text"] == player and
                self.buttons[row][2]["text"] == player):
                return True

        # Check columns
        for col in range(3):
            if (self.buttons[0][col]["text"] == player and
                self.buttons[1][col]["text"] == player and
                self.buttons[2][col]["text"] == player):
                return True

        # Check diagonals
        if (self.buttons[0][0]["text"] == player and
            self.buttons[1][1]["text"] == player and
            self.buttons[2][2]["text"] == player):
            return True

        if (self.buttons[0][2]["text"] == player and
            self.buttons[1][1]["text"] == player and
            self.buttons[2][0]["text"] == player):
            return True

        return False

    def is_board_full(self):
        """Check if all squares have been played (i.e., no empty buttons)."""
        for row in range(3):
            for col in range(3):
                if self.buttons[row][col]["text"] == "":
                    return False
        return True

    def reset_board(self):
        """Clear the board for a new game, but keep the current scores."""
        for row in range(3):
            for col in range(3):
                self.buttons[row][col].config(text="")
        self.current_player = "X"
        self.status_label.config(text=f"Current Player: {self.current_player}")

    def reset_scores(self):
        """Reset both players' scores to zero."""
        self.scores["X"] = 0
        self.scores["O"] = 0
        self.update_score_labels()
        self.reset_board()

    def update_score_labels(self):
        """Refresh the scoreboard labels to match current self.scores."""
        self.x_score_label.config(text=f"X Score: {self.scores['X']}")
        self.o_score_label.config(text=f"O Score: {self.scores['O']}")

def main():
    root = tk.Tk()
    game = TicTacToe(root)
    root.mainloop()

if __name__ == "__main__":
    main()
