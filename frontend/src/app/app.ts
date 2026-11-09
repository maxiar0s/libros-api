import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Book, BookInput, BookService } from './book.service';

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly booksApi = inject(BookService);
  private readonly fb = inject(FormBuilder);
  books: Book[] = [];
  selected: Book | null = null;
  editingId: number | null = null;
  busy = false;
  loading = false;
  hasLoaded = false;
  error = '';
  notice = '';
  searchId: number | null = null;
  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    author: ['', [Validators.required, Validators.maxLength(255)]],
    publicationYear: [null as number | null, [Validators.required, Validators.pattern(/^-?\d+$/)]],
    genre: ['', [Validators.required, Validators.maxLength(100)]]
  });
  readonly searchForm = this.fb.group({ id: [null as number | null, [Validators.required, Validators.min(1), Validators.pattern(/^[0-9]+$/)]] });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.error = '';
    this.booksApi.list().subscribe({
      next: books => { this.books = books; this.loading = false; this.hasLoaded = true; },
      error: error => { this.error = this.message(error); this.loading = false; }
    });
  }

  search(): void {
    if (this.searchForm.invalid || this.busy) { this.searchForm.markAllAsTouched(); return; }
    const id = this.searchForm.controls.id.value!;
    this.busy = true; this.error = ''; this.notice = ''; this.selected = null;
    this.booksApi.get(id).subscribe({
      next: book => { this.selected = book; this.busy = false; },
      error: error => { this.error = this.message(error); this.busy = false; }
    });
  }

  edit(book: Book): void {
    this.editingId = book.id;
    this.form.setValue({ title: book.title, author: book.author, publicationYear: book.publicationYear, genre: book.genre });
    this.error = ''; this.notice = '';
  }

  reset(): void {
    this.editingId = null;
    this.form.reset({ title: '', author: '', publicationYear: null, genre: '' });
  }

  save(): void {
    const value = this.form.getRawValue();
    if (this.form.invalid || !value.title.trim() || !value.author.trim() || !value.genre.trim() || this.busy) {
      this.form.markAllAsTouched();
      this.error = 'Completá todos los campos; no pueden contener solo espacios.';
      return;
    }
    const input: BookInput = { title: value.title.trim(), author: value.author.trim(), publicationYear: value.publicationYear!, genre: value.genre.trim() };
    const updating = this.editingId !== null;
    this.busy = true; this.error = ''; this.notice = '';
    const request = updating ? this.booksApi.update(this.editingId!, input) : this.booksApi.create(input);
    request.subscribe({
      next: book => { this.busy = false; this.notice = updating ? 'Libro actualizado.' : 'Libro agregado.'; this.selected = book; this.reset(); this.load(); },
      error: error => { this.busy = false; this.error = this.message(error); }
    });
  }

  remove(book: Book): void {
    if (this.busy || !window.confirm(`¿Eliminar «${book.title}»? Esta acción no se puede deshacer.`)) return;
    this.busy = true; this.error = ''; this.notice = '';
    this.booksApi.delete(book.id).subscribe({
      next: () => { this.busy = false; this.notice = 'Libro eliminado.'; if (this.selected?.id === book.id) this.selected = null; if (this.editingId === book.id) this.reset(); this.load(); },
      error: error => { this.busy = false; this.error = this.message(error); }
    });
  }

  private message(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 404) return 'No se encontró el libro solicitado.';
      if (error.status === 400) return 'Los datos ingresados no son válidos. Revisá los campos.';
    }
    return 'No se pudo completar la operación. Verificá que el microservicio esté disponible.';
  }
}
