import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';

describe('Library interface', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('displays the retrieved books and their attributes', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/books').flush([{ id: 1, title: 'El Hobbit', author: 'Tolkien', publicationYear: 1937, genre: 'Fantasía' }]);
    fixture.detectChanges();
    const page = fixture.nativeElement as HTMLElement;
    expect(page.textContent).toContain('El Hobbit');
    expect(page.textContent).toContain('Tolkien');
    expect(page.textContent).toContain('1937');
    expect(page.textContent).toContain('Fantasía');
    expect(page.textContent).toContain('1');
    http.verify();
  });
});
