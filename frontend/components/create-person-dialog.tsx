"use client"

import { useState, useRef } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { User, Upload, X } from "lucide-react"
import Image from "next/image"
import { cn } from "@/lib/utils"

export interface PersonFormData {
  firstName: string
  lastName: string
  initialLastName?: string
  gender: 'male' | 'female' | 'diverse'
  birthDate?: string
  deathDate?: string
  birthCity?: string
  birthCountry?: string
  email?: string
  telephone?: string
  streetNumber?: string
  plz?: string
  city?: string
  occupation?: string
  notes?: string
  imageUrl?: string
}

interface CreatePersonDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (data: PersonFormData) => void
  initialData?: Partial<PersonFormData>
  title?: string
}

export function CreatePersonDialog({ 
  open, 
  onOpenChange, 
  onSubmit, 
  initialData,
  title = "Create New Person"
}: CreatePersonDialogProps) {
  const [formData, setFormData] = useState<PersonFormData>({
    firstName: initialData?.firstName || '',
    lastName: initialData?.lastName || '',
    initialLastName: initialData?.initialLastName || '',
    gender: initialData?.gender || 'diverse',
    birthDate: initialData?.birthDate || '',
    deathDate: initialData?.deathDate || '',
    birthCity: initialData?.birthCity || '',
    birthCountry: initialData?.birthCountry || '',
    email: initialData?.email || '',
    telephone: initialData?.telephone || '',
    streetNumber: initialData?.streetNumber || '',
    plz: initialData?.plz || '',
    city: initialData?.city || '',
    occupation: initialData?.occupation || '',
    notes: initialData?.notes || '',
    imageUrl: initialData?.imageUrl || '',
  })
  
  const [errors, setErrors] = useState<{ firstName?: string; lastName?: string }>({})
  const [previewImage, setPreviewImage] = useState<string | null>(initialData?.imageUrl || null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  
  const updateField = <K extends keyof PersonFormData>(field: K, value: PersonFormData[K]) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    if (field === 'firstName' || field === 'lastName') {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }
  
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onloadend = () => {
        const result = reader.result as string
        setPreviewImage(result)
        updateField('imageUrl', result)
      }
      reader.readAsDataURL(file)
    }
  }
  
  const removeImage = () => {
    setPreviewImage(null)
    updateField('imageUrl', '')
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }
  
  const validate = (): boolean => {
    const newErrors: { firstName?: string; lastName?: string } = {}
    
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required'
    }
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }
  
  const handleSubmit = () => {
    if (validate()) {
      onSubmit(formData)
      // Reset form
      setFormData({
        firstName: '',
        lastName: '',
        initialLastName: '',
        gender: 'diverse',
        birthDate: '',
        deathDate: '',
        birthCity: '',
        birthCountry: '',
        email: '',
        telephone: '',
        streetNumber: '',
        plz: '',
        city: '',
        occupation: '',
        notes: '',
        imageUrl: '',
      })
      setPreviewImage(null)
      setErrors({})
      onOpenChange(false)
    }
  }
  
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="font-serif text-xl">{title}</DialogTitle>
          <DialogDescription>
            Fill in the details for the new family member. Fields marked with * are required.
          </DialogDescription>
        </DialogHeader>
        
        <div className="grid gap-4 py-4">
          {/* Image Upload */}
          <div className="bg-card rounded-xl border border-border p-5">
            <div className="flex items-start gap-4">
              <div 
                className={cn(
                  "w-20 h-20 rounded-xl border-2 border-dashed flex items-center justify-center shrink-0 overflow-hidden cursor-pointer transition-colors",
                  previewImage ? "border-terracotta" : "border-border hover:border-dusty-rose"
                )}
                onClick={() => fileInputRef.current?.click()}
              >
                {previewImage ? (
                  <Image 
                    src={previewImage} 
                    alt="Preview" 
                    width={80} 
                    height={80} 
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="text-center">
                    <Upload className="w-5 h-5 mx-auto text-muted-foreground mb-1" />
                    <span className="text-xs text-muted-foreground">Photo</span>
                  </div>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleImageUpload}
                className="hidden"
              />
              <div className="flex-1">
                <p className="text-sm font-medium text-foreground mb-1">Profile Photo</p>
                <p className="text-xs text-muted-foreground mb-2">JPG, PNG or GIF</p>
                {previewImage && (
                  <Button variant="outline" size="sm" onClick={removeImage} className="gap-1 h-7 text-xs">
                    <X className="w-3 h-3" />
                    Remove
                  </Button>
                )}
              </div>
            </div>
          </div>
          
          {/* Basic Info Section */}
          <div className="bg-card rounded-xl border border-border p-5">
            <h4 className="font-serif text-base text-foreground mb-4">Basic Information</h4>
            
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="firstName" className={cn("text-xs", errors.firstName ? 'text-destructive' : 'text-muted-foreground')}>
                    First Name *
                  </Label>
                  <Input
                    id="firstName"
                    value={formData.firstName}
                    onChange={(e) => updateField('firstName', e.target.value)}
                    placeholder="First name"
                    className={cn("h-9", errors.firstName ? 'border-destructive' : '')}
                  />
                  {errors.firstName && (
                    <p className="text-xs text-destructive">{errors.firstName}</p>
                  )}
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="lastName" className={cn("text-xs", errors.lastName ? 'text-destructive' : 'text-muted-foreground')}>
                    Last Name *
                  </Label>
                  <Input
                    id="lastName"
                    value={formData.lastName}
                    onChange={(e) => updateField('lastName', e.target.value)}
                    placeholder="Last name"
                    className={cn("h-9", errors.lastName ? 'border-destructive' : '')}
                  />
                  {errors.lastName && (
                    <p className="text-xs text-destructive">{errors.lastName}</p>
                  )}
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="initialLastName" className="text-xs text-muted-foreground">Birth Name</Label>
                  <Input
                    id="initialLastName"
                    value={formData.initialLastName}
                    onChange={(e) => updateField('initialLastName', e.target.value)}
                    placeholder="If different"
                    className="h-9"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="gender" className="text-xs text-muted-foreground">Gender</Label>
                  <Select 
                    value={formData.gender} 
                    onValueChange={(value) => updateField('gender', value as 'male' | 'female' | 'diverse')}
                  >
                    <SelectTrigger className="w-full h-9">
                      <SelectValue placeholder="Select" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="male">Male</SelectItem>
                      <SelectItem value="female">Female</SelectItem>
                      <SelectItem value="diverse">Diverse</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="birthDate" className="text-xs text-muted-foreground">Birth Date</Label>
                  <Input
                    id="birthDate"
                    type="date"
                    value={formData.birthDate}
                    onChange={(e) => updateField('birthDate', e.target.value)}
                    className="h-9"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="deathDate" className="text-xs text-muted-foreground">Death Date</Label>
                  <Input
                    id="deathDate"
                    type="date"
                    value={formData.deathDate}
                    onChange={(e) => updateField('deathDate', e.target.value)}
                    className="h-9"
                  />
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="birthCity" className="text-xs text-muted-foreground">Birth City</Label>
                  <Input
                    id="birthCity"
                    value={formData.birthCity}
                    onChange={(e) => updateField('birthCity', e.target.value)}
                    placeholder="City"
                    className="h-9"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="birthCountry" className="text-xs text-muted-foreground">Birth Country</Label>
                  <Input
                    id="birthCountry"
                    value={formData.birthCountry}
                    onChange={(e) => updateField('birthCountry', e.target.value)}
                    placeholder="Country"
                    className="h-9"
                  />
                </div>
              </div>
              
              <div className="space-y-1.5">
                <Label htmlFor="occupation" className="text-xs text-muted-foreground">Occupation</Label>
                <Input
                  id="occupation"
                  value={formData.occupation}
                  onChange={(e) => updateField('occupation', e.target.value)}
                  placeholder="What do/did they do?"
                  className="h-9"
                />
              </div>
            </div>
          </div>
          
          {/* Contact Info Section */}
          <div className="bg-card rounded-xl border border-border p-5">
            <h4 className="font-serif text-base text-foreground mb-4">Contact</h4>
            
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="email" className="text-xs text-muted-foreground">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => updateField('email', e.target.value)}
                    placeholder="email@example.com"
                    className="h-9"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="telephone" className="text-xs text-muted-foreground">Phone</Label>
                  <Input
                    id="telephone"
                    type="tel"
                    value={formData.telephone}
                    onChange={(e) => updateField('telephone', e.target.value)}
                    placeholder="+1 234 567 8900"
                    className="h-9"
                  />
                </div>
              </div>
              
              <div className="space-y-1.5">
                <Label htmlFor="streetNumber" className="text-xs text-muted-foreground">Street Address</Label>
                <Input
                  id="streetNumber"
                  value={formData.streetNumber}
                  onChange={(e) => updateField('streetNumber', e.target.value)}
                  placeholder="123 Main Street"
                  className="h-9"
                />
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="plz" className="text-xs text-muted-foreground">Postal Code</Label>
                  <Input
                    id="plz"
                    value={formData.plz}
                    onChange={(e) => updateField('plz', e.target.value)}
                    placeholder="12345"
                    className="h-9"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="city" className="text-xs text-muted-foreground">City</Label>
                  <Input
                    id="city"
                    value={formData.city}
                    onChange={(e) => updateField('city', e.target.value)}
                    placeholder="City"
                    className="h-9"
                  />
                </div>
              </div>
            </div>
          </div>
          
          {/* Notes Section */}
          <div className="bg-card rounded-xl border border-border p-5">
            <h4 className="font-serif text-base text-foreground mb-4">Notes</h4>
            <Textarea
              id="notes"
              value={formData.notes}
              onChange={(e) => updateField('notes', e.target.value)}
              placeholder="Add any additional notes about this person..."
              className="min-h-20 resize-none"
            />
          </div>
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button 
            onClick={handleSubmit}
            className="bg-terracotta text-parchment hover:bg-terracotta/90"
          >
            Create Person
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
